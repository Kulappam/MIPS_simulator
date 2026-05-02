package mips.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import mips.app.SimulationController;
import mips.core.ParsedCommand;
import mips.gui.component.ToggleButton;
import mips.gui.view.*;

import javax.swing.*;

public class MainWindow {
    private final Stage stage;
    private SimulationController controller;

    private EditorView editorView;
    private RegisterView registerView;
    private MemoryView memoryView;
    private ConsoleView consoleView;
    private DataPathView dataPathView;

    ToggleButton toggleButton;
    private Label statusLabel;
    private Slider speedSlider;

    public MainWindow(Stage stage, SimulationController controller) {
        this.stage = stage;
        this.controller = controller;
        initInterface();

        if (controller != null) {
            controller.addStateListener(this::onSimulatorStateChanged);
        }
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
    }

    private void initInterface() {
        stage.setTitle("MIPS Simulator");

        // ========== КОМПОНЕНТЫ ==========
        editorView = new SimpleEditorView(new TextArea());
        registerView = new RegisterView();
        memoryView = new MemoryView();
        consoleView = new ConsoleView();
        dataPathView = new DataPathView();

        // ========== MENU BAR ==========
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem loadItem = new MenuItem("Load...");
        MenuItem saveItem = new MenuItem("Save...");
        SeparatorMenuItem sep = new SeparatorMenuItem();
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newItem, loadItem, saveItem, sep, exitItem);

        Menu helperMenu = new Menu("Helper");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem helpItem = new MenuItem("Help");
        helperMenu.getItems().addAll(aboutItem, helpItem);

        Menu exercisesMenu = new Menu("Exercises");
        MenuItem ex1Item = new MenuItem("Arithmetic");
        MenuItem ex2Item = new MenuItem("Memory");
        MenuItem ex3Item = new MenuItem("Loops");
        exercisesMenu.getItems().addAll(ex1Item, ex2Item, ex3Item);

        menuBar.getMenus().addAll(fileMenu, helperMenu, exercisesMenu);

        // ========== TOOL BAR ==========
        toggleButton = new ToggleButton();
        Button stepButton = new Button("■ STEP");
        Button resetButton = new Button("↺ RESET");

        stepButton.getStyleClass().add("step-button");
        resetButton.getStyleClass().add("reset-button");

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(toggleButton, stepButton, resetButton);

        // ========== ВЕРХНЯЯ ПАНЕЛЬ ==========
        BorderPane topPane = new BorderPane();
        topPane.setLeft(menuBar);
        topPane.setRight(toolBar);
        topPane.getStyleClass().add("top-pane");

        // ========== ЛЕВАЯ ПАНЕЛЬ (Editor + DataPath) ==========
        TabPane leftTabPane = new TabPane();

        Tab editorTab = new Tab("Editor", editorView.getView());
        Tab datapathTab = new Tab("DataPath", dataPathView.getView());

        editorTab.setClosable(false);
        datapathTab.setClosable(false);

        leftTabPane.getTabs().addAll(editorTab, datapathTab);
        leftTabPane.getStyleClass().add("left-tab-pane");

        // ========== ПРАВАЯ ПАНЕЛЬ (Registers + Memory) ==========
        TabPane rightTabPane = new TabPane();

        Tab registersTab = new Tab("Registers", registerView.getView());
        Tab memoryTab = new Tab("Memory", memoryView.getView());

        registersTab.setClosable(false);
        memoryTab.setClosable(false);

        rightTabPane.getTabs().addAll(registersTab, memoryTab);
        rightTabPane.getStyleClass().add("right-tab-pane");

        // ========== ЦЕНТР (SplitPane) ==========
        SplitPane centerSplit = new SplitPane();
        centerSplit.getItems().addAll(leftTabPane, rightTabPane);
        centerSplit.setDividerPositions(0.5);
        centerSplit.getStyleClass().add("center-split");

        // ========== НИЗ (Console) ==========
        VBox bottomPane = new VBox();
        bottomPane.getChildren().add(consoleView.getView());
        bottomPane.getStyleClass().add("bottom-pane");

        // ========== КОРНЕВОЙ КОНТЕЙНЕР ==========
        BorderPane root = new BorderPane();
        root.setTop(topPane);
        root.setCenter(centerSplit);
        root.setBottom(bottomPane);
        root.getStyleClass().add("root-pane");

        // ========== ДЕЙСТВИЯ КНОПОК ==========
        toggleButton.setRunAction(() -> {
            if (controller != null) controller.onRun();
        });
        toggleButton.setStopAction(() -> {
            if (controller != null) controller.onStop();
        });

        stepButton.setOnAction(e -> {
            if (controller != null) controller.onStep();
        });
        resetButton.setOnAction(e -> {
            if (controller != null) controller.onReset();
        });

        // Menu actions
        newItem.setOnAction(e -> {
            if (controller != null) controller.onReset();
            editorView.setText("");
            consoleView.clear();
        });

        exitItem.setOnAction(e -> stage.close());

        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("MIPS Simulator");
            alert.setContentText("Educational MIPS Simulator\nCreated for Diploma Project");
            alert.showAndWait();
        });

        // ========== СЦЕНА ==========
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    // ==================== МЕТОДЫ ДЛЯ КОНТРОЛЛЕРА ====================

    public String getEditorText() {
        return editorView.getText();
    }

    public void updateRegisters(int[] values) {
        registerView.update(values);
    }

    public void highlightLine(int pc) {
        int lineIndex = (pc - 0x00400000) / 4;
        if (lineIndex >= 0) {
            editorView.highLightLine(lineIndex);
        } else {
            editorView.clearHighLights();
        }
    }

    public void updatePC(int pc) {
        if (statusLabel != null) {
            statusLabel.setText(String.format("PC: 0x%08X", pc));
        }
    }

    public void consoleLog(String message) {
        consoleView.log(message);
    }

    public void clearConsole() {
        consoleView.clear();
    }

    public void show() {
        stage.show();
    }

    // ==================== DATAPATH МЕТОДЫ ====================

    public void onFetch(int pc, ParsedCommand command) {
        updatePC(pc);
        dataPathView.onFetch(command);
        if (command != null) {
            consoleLog("FETCH: " + command.type());
        }
    }

    public void onDecode(ParsedCommand command) {
        dataPathView.onDecode(command);
        if (command != null) {
            consoleLog("DECODE: " + command.type());
        }
    }

    public void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation) {
        dataPathView.onExecute(aluOperand1, aluOperand2, aluResult, operation);
        consoleLog("EXECUTE: " + operation + " " + aluOperand1 + ", " + aluOperand2 + " → " + aluResult);
    }

    public void onMemoryAccess(int address, int value, boolean isRead) {
        dataPathView.onMemoryAccess(address, value, isRead);
        String op = isRead ? "READ" : "WRITE";
        consoleLog("MEMORY " + op + ": 0x" + Integer.toHexString(address) + " = " + value);
    }

    public void onWriteBack(int registerIndex, int value) {
        dataPathView.onWriteBack(registerIndex, value);
        consoleLog("WRITEBACK: $" + registerIndex + " = " + value);
    }

    public void updateMemoryCell(int address, int value) {
        memoryView.updateSingleAddress(address, value);
    }

    public void refreshMemoryDump() {
        if (memoryView != null && controller != null) {
            memoryView.updateMemory(controller.getMemoryBytes(), 0x10010000);
        }
    }

    public void onInstructionExecuted(ParsedCommand command) {
        if (command != null) {
            consoleLog("Executed: " + command.type());
        }
    }

    public void onSimulatorStateChanged(SimulationController.SimulatorState state) {
        Platform.runLater(() -> {
            switch (state) {
                case RUNNING:
                    toggleButton.run();
                    statusLabel.setText("Running...");
                    break;
                case IDLE:
                    toggleButton.stop();
                    statusLabel.setText("Ready");
                    dataPathView.reset();
                    break;
                case HALTED:
                    toggleButton.reset();
                    statusLabel.setText("Program halted");
                    dataPathView.reset();
                    break;
            }
        });
    }
}
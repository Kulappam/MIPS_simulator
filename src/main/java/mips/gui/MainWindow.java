package mips.gui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import mips.app.ErrorHandler;
import mips.app.SimulationController;
import mips.core.*;
import mips.gui.view.*;
import mips.exceptions.ParsingException;

import java.util.List;

public class MainWindow {
    private final Stage stage;
    private final SimulationController controller;
    private final Parser parser;
    private final Memory memory;

    private final EditorView editorView;
    private final RegisterView registerView;
    private final MemoryView memoryView;
    private final ConsoleView consoleView;
    private final DataPath dataPathView;

    private final Button runButton, stepButton, stopButton, resetButton;
    private final Slider speedSlider;

    public MainWindow(Stage stage, SimulationController controller,
                      Parser parser, Memory memory,
                      EditorView editorView,
                      RegisterView registerView,
                      MemoryView memoryView,
                      ConsoleView consoleView,
                      DataPath dataPathView) {
        this.stage = stage;
        this.controller = controller;
        this.parser = parser;
        this.memory = memory;
        this.editorView = editorView;
        this.registerView = registerView;
        this.memoryView = memoryView;
        this.consoleView = consoleView;
        this.dataPathView = dataPathView;

        this.runButton = new Button("RUN");
        this.stepButton = new Button("STEP");
        this.stopButton = new Button("STOP");
        this.resetButton = new Button("RESET");
        this.speedSlider = new Slider(0, 500, 100);

        initInterface();

        if (controller != null) {
            controller.addStateListener(this::onControllerStateChanged);
        }
    }

    private void initInterface() {
        stage.setTitle("MIPS Simulator");

        runButton.setStyle("-fx-font-weight: bold;");
        stepButton.setStyle("-fx-font-weight: bold;");
        stopButton.setStyle("-fx-font-weight: bold;");
        resetButton.setStyle("-fx-font-weight: bold;");

        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setMinorTickCount(4);
        speedSlider.setMaxWidth(120);
        Label speedLabel = new Label("Delay:");

        ToolBar toolBar = new ToolBar(runButton, stopButton, stepButton, resetButton,
                new Separator(), speedLabel, speedSlider);

        MenuBar menuBar = createMenuBar();

        BorderPane topPane = new BorderPane();
        topPane.setLeft(menuBar);
        topPane.setRight(toolBar);
        topPane.getStyleClass().add("top-pane");

        TabPane leftTabPane = new TabPane();
        leftTabPane.getTabs().addAll(
                new Tab("Editor", editorView.getView()),
                new Tab("DataPath", dataPathView.getView())
        );
        leftTabPane.getTabs().forEach(tab -> tab.setClosable(false));

        TabPane rightTabPane = new TabPane();
        rightTabPane.getTabs().addAll(
                new Tab("Registers", registerView.getView()),
                new Tab("Memory", memoryView.getView())
        );
        rightTabPane.getTabs().forEach(tab -> tab.setClosable(false));
        rightTabPane.setMinWidth(400);
        rightTabPane.setMaxWidth(400);

        SplitPane centerSplit = new SplitPane(leftTabPane, rightTabPane);
        centerSplit.setDividerPositions(0.5);

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalSplit.getItems().addAll(centerSplit, consoleView.getView());
        verticalSplit.setDividerPositions(0.7); // 70% на центр, 30% на консоль
        verticalSplit.setStyle("-fx-background-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(topPane);
        root.setCenter(verticalSplit);

        // Действия кнопок
        runButton.setOnAction(e -> { loadProgram(); controller.run(); });
        stepButton.setOnAction(e -> { loadProgram(); controller.step(); });
        stopButton.setOnAction(e -> controller.stop());
        resetButton.setOnAction(e -> { controller.reset(); memory.reset(); consoleView.clear(); });

        speedSlider.valueProperty().addListener((obs, old, val) ->
                controller.setStepDelay(val.intValue()));

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ========== FILE MENU ==========
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> {
            editorView.setText("");
            consoleView.clear();
            memory.reset();
            controller.reset();
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(newItem, new SeparatorMenuItem(), exitItem);

        // ========== EXERCISES MENU ==========
        Menu exercisesMenu = new Menu("Exercises");
        MenuItem ex1 = new MenuItem("Sum 1 to 10");
        ex1.setOnAction(e -> loadExercise(getSum1To10Code()));
        MenuItem ex2 = new MenuItem("Memory Store/Load");
        ex2.setOnAction(e -> loadExercise(getMemoryStoreLoadCode()));
        exercisesMenu.getItems().addAll(ex1, ex2);

        // ========== HELP MENU ==========
        Menu helpMenu = new Menu("Help");
        MenuItem helpItem = new MenuItem("Help");
        helpItem.setOnAction(e -> showHelpDialog());
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().addAll(helpItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, exercisesMenu, helpMenu);
        return menuBar;
    }

    private void loadExercise(String code) {
        editorView.setText(code);
        consoleLog("[INFO] Exercise loaded. Press RUN or STEP to execute.");
    }

    private void showHelpDialog() {
        String content = """
        MIPS SIMULATOR – QUICK HELP
        
        INSTRUCTIONS IMPLEMENTED:
        • li Rd, imm   – Load immediate value into register
        • add Rd, Rs, Rt – Rd = Rs + Rt
        • sub Rd, Rs, Rt – Rd = Rs - Rt
        • lw Rt, offset(Rs) – Load word from memory
        • sw Rt, offset(Rs) – Store word to memory
        • syscall – System call (only exit = 10 supported)
        
        DATA PATH:
        1. FETCH   – Read instruction from IMEM using PC
        2. DECODE  – Read registers from RegFile, sign-extend immediate
        3. EXECUTE – ALU computes result
        4. MEMORY  – Read/Write Data Memory
        5. WRITEBACK – Write result back to Register File
        
        TIPS:
        • Use STEP to execute one instruction at a time
        • RUN runs the whole program (adjust speed with slider)
        • STOP halts execution
        • RESET clears registers and program counter
        • Memory data starts at address 0x10010000
        """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help – MIPS Simulator");
        alert.setHeaderText("MIPS Architecture Reference");
        alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    private void showAboutDialog() {
        String content = """
        MIPS SIMULATOR
        Version 1.0
        
        Educational MIPS Assembler Simulator
        Developed as a Diploma Project
        
        Features:
        • Editor with syntax highlighting
        • Step-by-step execution
        • Registers and memory viewer
        • DataPath visualization
        • Interactive exercises
        • Console with colored output
        
        Built with JavaFX and RichTextFX.
        """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About MIPS Simulator");
        alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }

    private void loadProgram() {
        try {
            String text = editorView.getText();
            List<ParsedCommand> program = parser.parseText(text);
            memory.loadProgram(program, Memory.TEXT_START);
        } catch (ParsingException e) {
            ErrorHandler.reportError(e);
        }
    }

    private void onControllerStateChanged(SimulationController.ControllerState state) {
        Platform.runLater(() -> {
            boolean isRunning = (state == SimulationController.ControllerState.RUNNING);
            runButton.setDisable(isRunning);
            stopButton.setDisable(!isRunning);
            stepButton.setDisable(isRunning);
        });
    }

    public void consoleLog(String message) {
        if (message.startsWith("[ERROR]")) {
            consoleView.logError(message);
        } else if (message.startsWith("[SYSCALL]")) {
            consoleView.logSyscall(message);
        } else if (message.startsWith("[INFO]")) {
            consoleView.logInfo(message);
        } else {
            consoleView.logInfo(message);
        }
    }

    private String getSum1To10Code() {
        return """
        # Exercise: Sum of numbers from 1 to 10
        # Expected result: $t0 = 55
        
        li $t0, 0          # sum = 0
        
        # 1
        li $t1, 1
        add $t0, $t0, $t1
        # 2
        li $t1, 2
        add $t0, $t0, $t1
        # 3
        li $t1, 3
        add $t0, $t0, $t1
        # 4
        li $t1, 4
        add $t0, $t0, $t1
        # 5
        li $t1, 5
        add $t0, $t0, $t1
        # 6
        li $t1, 6
        add $t0, $t0, $t1
        # 7
        li $t1, 7
        add $t0, $t0, $t1
        # 8
        li $t1, 8
        add $t0, $t0, $t1
        # 9
        li $t1, 9
        add $t0, $t0, $t1
        # 10
        li $t1, 10
        add $t0, $t0, $t1
        
        li $v0, 10
        syscall
        """;
    }

    private String getMemoryStoreLoadCode() {
        return """
        # Exercise: Memory store/load
        # Store 42 to address 0x10010000, then load back to $t0
        # Expected: $t0 = 42
        
        li $t1, 42
        li $t2, 0x10010000
        sw $t1, 0($t2)
        lw $t0, 0($t2)
        
        li $v0, 10
        syscall
        """;
    }

    public void show() {
        stage.show();
    }
}
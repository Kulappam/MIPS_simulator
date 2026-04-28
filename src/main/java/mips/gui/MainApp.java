package mips.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import mips.core.Cpu;
import mips.core.Parser;

public class MainApp extends Application {
    private final Cpu cpu = new Cpu();
    private final Parser parser = new Parser();

    // Элементы интерфейса
    private CodeEditor editor;
    private RegisterTableComponent registerComponent;
    private Button buttonRUN;
    private Button buttonRESET;
    private Button buttonSTEP;

    @Override
    public void start(Stage stage) {
        stage.setTitle("MIPS Simulator - Alpha");

        // Редактор кода в левой части
        TextArea rawTextArea = new TextArea();
        rawTextArea.setPromptText("Введите код MIPS здесь... (например: li $1, 10)");
        rawTextArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        editor = new SimpleCodeEditor(rawTextArea);

        VBox leftPane = new VBox(new Label("РЕДАКТОР"), editor.getView());
        leftPane.setPadding(new Insets(10));
        VBox.setVgrow(editor.getView(), Priority.ALWAYS);

        registerComponent = new RegisterTableComponent();

        // Кнопки в нижней части
        buttonRUN = new Button("RUN");
        buttonSTEP = new Button("STEP");
        buttonRESET = new Button("RESET");

        buttonSTEP.setOnAction(e -> runStep());
        buttonRESET.setOnAction(e -> {
            cpu.reset();
            registerComponent.update(cpu.getRegisters().getAll());
        });

        HBox controls = new HBox(10, buttonRUN, buttonSTEP, buttonRESET);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: #f0f0f0;");

        // Сборка всего в один лейаут
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(leftPane);
        mainLayout.setRight(registerComponent.getView());
        mainLayout.setBottom(controls);

        Scene scene = new Scene(mainLayout, 900, 600);
        stage.setScene(scene);
        stage.show();
    }


    private void runStep() {
        String[] lines = editor.getText().split("\n");

        //Проверка выхода за границы кода
        if (cpu.getPc() < lines.length) {
            String currentLine = lines[cpu.getPc()].trim();

            // Выполняем непустые строки
            if(!currentLine.isEmpty()) {
                editor.highLightLine(cpu.getPc());

                try {
                    parser.parseAndExecute(cpu, currentLine);
                    registerComponent.update(cpu.getRegisters().getAll());
                } catch (Exception e) {
                    System.err.println("Error in string " + (cpu.getPc() + 1));
                }
            }

            cpu.advancePc();
        } else {
            System.out.println("Program ended (PC is out of text)");
            editor.clearHighLights();
        }
    }

    public static void main(String[] args) { launch(); }
}
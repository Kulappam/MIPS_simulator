package mips.app;

import javafx.application.Application;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import mips.core.*;
import mips.gui.MainWindow;
import mips.gui.view.*;

public class MipsSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        // ========== 1. СОЗДАЁМ CORE ==========
        Memory memory = new Memory();
        RegisterFile registers = new RegisterFile();
        Cpu cpu = new Cpu(memory, registers);
        Parser parser = new Parser();

        // ========== 2. СОЗДАЁМ ВЬЮШКИ ==========
        RegisterView registerView = new RegisterView();
        MemoryView memoryView = new MemoryView();
        //DataPathView dataPathView = new DataPathView();
        CanvasDataPathView dataPathView = new CanvasDataPathView();
        ConsoleView consoleView = new ConsoleView();
        EditorView editorView = new EditorView();

        // ========== 3. ПОДПИСЫВАЕМ ВЬЮШКИ ==========
        cpu.addListener(registerView);
        cpu.addListener(dataPathView);
        cpu.getMemory().addListener(memoryView);

        // ========== 4. СОЗДАЁМ КОНТРОЛЛЕР ==========
        SimulationController controller = new SimulationController(cpu);

        // ========== 5. СОЗДАЁМ ОКНО ==========
        MainWindow window = new MainWindow(
                stage,
                controller,
                parser,
                memory,
                editorView,
                registerView,
                memoryView,
                consoleView,
                dataPathView
        );

        ErrorHandler.setConsoleLogger(window::consoleLog);

        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
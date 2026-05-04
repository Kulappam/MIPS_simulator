package mips.app;

import javafx.application.Application;
import javafx.stage.Stage;
import mips.core.*;
import mips.gui.MainWindow;
import mips.gui.view.*;

public class MipsSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        Memory memory = new Memory();
        RegisterFile registers = new RegisterFile();
        Cpu cpu = new Cpu(memory, registers);
        Parser parser = new Parser();

        RegisterView registerView = new RegisterView();
        MemoryView memoryView = new MemoryView();
        DataPathView dataPathView = new DataPathView();
        //CanvasDataPathView dataPathView = new CanvasDataPathView();
        ConsoleView consoleView = new ConsoleView();
        EditorView editorView = new EditorView();

        cpu.addListener(registerView);
        cpu.addListener(dataPathView);
        cpu.getMemory().addListener(memoryView);

        SimulationController controller = new SimulationController(cpu);

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
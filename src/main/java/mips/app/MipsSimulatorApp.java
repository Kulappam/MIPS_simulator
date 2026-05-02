package mips.app;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.stage.Stage;
import mips.core.*;
import mips.gui.MainWindow;

public class MipsSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        // ========== 1. СОЗДАЕМ CORE КОМПОНЕНТЫ ==========
        Memory memory = new Memory();
        RegisterFile registers = new RegisterFile();
        Cpu cpu = new Cpu(memory, registers);
        Parser parser = new Parser();

        MainWindow window = new MainWindow(stage, null);

        SimulationController controller = new SimulationController(cpu, parser, window);

        window.setController(controller);

        ErrorHandler.setConsoleLogger(window::consoleLog);

        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
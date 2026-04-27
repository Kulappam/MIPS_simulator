package mips.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mips.core.Cpu;
import mips.core.Parser;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Создаем область для вывода текста
        TextArea logArea = new TextArea();
        logArea.setEditable(false); // Запрещаем редактирование
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");

        // Инициализируем наше "ядро"
        Cpu cpu = new Cpu();
        Parser parser = new Parser();

        // Пишем тестовый лог
        StringBuilder output = new StringBuilder();
        output.append("--- Тест системы MIPS ---\n");

        // Выполняем команды
        try {
            parser.parseAndExecute(cpu, "li $1, 10");
            output.append("Выполнено: li $1, 10\n");

            parser.parseAndExecute(cpu, "li $2, 25");
            output.append("Выполнено: li $2, 25\n");

            parser.parseAndExecute(cpu, "add $3, $1, $2");
            output.append("Выполнено: add $3, $1, $2\n");

            // Считываем результат
            int result = cpu.getRegisters().read(3);
            output.append("\nРезультат в регистре $3: ").append(result).append("\n");

            if (result == 35) {
                output.append("СТАТУС: ТЕСТ ПРОЙДЕН (10 + 25 = 35)");
            } else {
                output.append("СТАТУС: ОШИБКА ВЫЧИСЛЕНИЙ");
            }

        } catch (Exception e) {
            output.append("\nОшибка при выполнении: ").append(e.getMessage());
        }

        // Выводим накопленный текст в окно
        logArea.setText(output.toString());

        // Настройка интерфейса
        BorderPane root = new BorderPane();
        root.setCenter(logArea);

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("MIPS Kernel Test (Java 26)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
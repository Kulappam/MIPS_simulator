package mips.app;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.function.Consumer;

public class ErrorHandler {

    private static Consumer<String> consoleLogger = System.err::println;

    /**
     * Установить логгер для вывода сообщений в GUI консоль
     * @param logger логгер (например, MainWindow::consoleLog)
     */
    public static void setConsoleLogger(Consumer<String> logger) {
        if (logger != null) {
            consoleLogger = logger;
        }
    }

    public static void reportError(RuntimeException e) {
        String type = e.getClass().getSimpleName();
        String message = e.getMessage();
        String fullMessage = "ERROR [" + type + "]: " + message;

        // Логируем через установленный логгер (в GUI консоль или System.err)
        consoleLogger.accept(fullMessage);

        // Показываем диалоговое окно
        showDialog("MipsSimulator: " + type, message);
    }

    public static void reportGenericError(Exception e) {
        String type = e.getClass().getSimpleName();
        String message = e.getMessage();
        String fullMessage = "ERROR [" + type + "]: " + message;

        consoleLogger.accept(fullMessage);

        showDialog("System: " + type, message);
    }

    public static void reportMessage(String message) {
        consoleLogger.accept(message);
    }

    private static void showDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
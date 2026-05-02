package mips.app;

import java.util.function.Consumer;

public class ErrorHandler {

    private static Consumer<String> consoleLogger = System.out::println;

    public static void setConsoleLogger(Consumer<String> infoLogger) {
        if (infoLogger != null) consoleLogger = infoLogger;
    }

    public static void reportError(RuntimeException e) {
        String type = e.getClass().getSimpleName();
        String message = e.getMessage();
        String fullMessage = "[ERROR] " + type + ": " + message;

        consoleLogger.accept(fullMessage);
    }

    public static void reportInfo(String message) {
        consoleLogger.accept("[INFO] " + message);
    }

    public static void reportSyscall(String message) {
        consoleLogger.accept("[SYSCALL] " + message);
    }

}
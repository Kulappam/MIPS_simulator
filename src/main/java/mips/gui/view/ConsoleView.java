package mips.gui.view;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ConsoleView {
    private final VBox consoleContent; // VBox для накопления строк
    private final ScrollPane scrollPane;
    private final VBox root;

    public ConsoleView() {
        this.consoleContent = new VBox();
        this.consoleContent.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 5;");
        this.consoleContent.setFillWidth(true);

        this.scrollPane = new ScrollPane(consoleContent);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setPrefHeight(150);
        this.scrollPane.setStyle("-fx-background: #f9f9f9;");
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Label title = new Label("CONSOLE");
        title.getStyleClass().add("console-title");

        this.root = new VBox(5);
        this.root.getChildren().addAll(title, scrollPane);
        this.root.getStyleClass().add("console-view");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void appendColored(String message, String colorHex) {
        Platform.runLater(() -> {
            Text text = new Text("> " + message + "\n");
            text.setStyle("-fx-fill: " + colorHex + ";");
            consoleContent.getChildren().add(text);
            // Автопрокрутка вниз
            scrollPane.setVvalue(1.0);
        });
    }

    public void logInfo(String message) {
        appendColored(message, "#000000");
    }

    public void logError(String message) {
        appendColored(message, "#ff0000");
    }

    public void logSyscall(String message) {
        appendColored(message, "#800080");
    }

    public void logWarning(String message) {
        appendColored(message, "#ffa500");
    }

    public void clear() {
        Platform.runLater(() -> consoleContent.getChildren().clear());
    }

    public Node getView() {
        return root;
    }
}
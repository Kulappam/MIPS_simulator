package mips.gui.view;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConsoleView {
    private final TextArea consoleArea;
    private final VBox root;

    public ConsoleView() {
        this.consoleArea = new TextArea();
        this.consoleArea.setEditable(false);
        this.consoleArea.getStyleClass().add("console-area");

        Label title = new Label("CONSOLE");
        title.getStyleClass().add("console-title");

        this.root = new VBox(5);
        this.root.getChildren().addAll(title, consoleArea);
        this.root.getStyleClass().add("console-view");

        VBox.setVgrow(consoleArea, Priority.ALWAYS);
    }

    public void log(String message) {
        Platform.runLater(() -> {
            consoleArea.appendText("> " + message + "\n");
            consoleArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void clear() {
        Platform.runLater(() -> consoleArea.clear());
    }

    public Node getView() {
        return root;
    }
}
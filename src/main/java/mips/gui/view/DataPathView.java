package mips.gui.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mips.core.ParsedCommand;

public class DataPathView {
    private final VBox root;
    private final Label statusLabel;
    private final Label fetchLabel;
    private final Label decodeLabel;
    private final Label executeLabel;
    private final Label memoryLabel;
    private final Label writebackLabel;

    public DataPathView() {
        this.root = new VBox(15);
        this.root.setAlignment(Pos.CENTER);
        this.root.getStyleClass().add("datapath-view");

        Label title = new Label("DATA PATH VISUALIZATION");
        title.getStyleClass().add("datapath-title");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("datapath-status");

        fetchLabel = createStageLabel("FETCH", "Waiting...");
        decodeLabel = createStageLabel("DECODE", "Waiting...");
        executeLabel = createStageLabel("EXECUTE", "Waiting...");
        memoryLabel = createStageLabel("MEMORY", "Waiting...");
        writebackLabel = createStageLabel("WRITEBACK", "Waiting...");

        VBox stagesBox = new VBox(5);
        stagesBox.getChildren().addAll(
                fetchLabel, decodeLabel, executeLabel, memoryLabel, writebackLabel
        );
        stagesBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, statusLabel, stagesBox);
        root.setMinHeight(300);
    }

    private Label createStageLabel(String stageName, String initialValue) {
        Label label = new Label(stageName + ": " + initialValue);
        label.getStyleClass().add("datapath-stage");
        return label;
    }

    public void onFetch(ParsedCommand command) {
        resetAllStages();
        fetchLabel.setText("FETCH: " + (command != null ? command.type() : "null"));
        fetchLabel.getStyleClass().add("datapath-stage-active");
        scheduleClearHighlight(fetchLabel);
        statusLabel.setText("Executing...");
    }

    public void onDecode(ParsedCommand command) {
        decodeLabel.setText("DECODE: " + (command != null ? command.type() : "null"));
        decodeLabel.getStyleClass().add("datapath-stage-active");
        scheduleClearHighlight(decodeLabel);
    }

    public void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation) {
        executeLabel.setText("EXECUTE: " + operation + " " + aluOperand1 + ", " + aluOperand2 + " → " + aluResult);
        executeLabel.getStyleClass().add("datapath-stage-active");
        scheduleClearHighlight(executeLabel);
    }

    public void onMemoryAccess(int address, int value, boolean isRead) {
        String op = isRead ? "READ" : "WRITE";
        memoryLabel.setText("MEMORY " + op + ": 0x" + Integer.toHexString(address) + " = " + value);
        memoryLabel.getStyleClass().add("datapath-stage-active");
        scheduleClearHighlight(memoryLabel);
    }

    public void onWriteBack(int registerIndex, int value) {
        writebackLabel.setText("WRITEBACK: $" + registerIndex + " = " + value);
        writebackLabel.getStyleClass().add("datapath-stage-active");
        scheduleClearHighlight(writebackLabel);
    }

    public void reset() {
        statusLabel.setText("Ready");
        fetchLabel.setText("FETCH: Waiting...");
        decodeLabel.setText("DECODE: Waiting...");
        executeLabel.setText("EXECUTE: Waiting...");
        memoryLabel.setText("MEMORY: Waiting...");
        writebackLabel.setText("WRITEBACK: Waiting...");

        resetAllStages();
    }

    private void scheduleClearHighlight(Label label) {
        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(300),
                        e -> {
                            if (label.getStyleClass().contains("datapath-stage-active")) {
                                label.getStyleClass().remove("datapath-stage-active");
                            }
                        }
                )
        ).play();
    }

    private void resetAllStages() {
        fetchLabel.getStyleClass().remove("datapath-stage-active");
        decodeLabel.getStyleClass().remove("datapath-stage-active");
        executeLabel.getStyleClass().remove("datapath-stage-active");
        memoryLabel.getStyleClass().remove("datapath-stage-active");
        writebackLabel.getStyleClass().remove("datapath-stage-active");
    }

    public Node getView() {
        return root;
    }
}
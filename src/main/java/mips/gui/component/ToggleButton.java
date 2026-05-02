package mips.gui.component;

import javafx.scene.control.Button;

/**
 * Кнопка с двумя состояниями: RUN и STOP.
 * Автоматически меняет текст, стиль и поведение.
 */
public class ToggleButton extends Button {

    private Runnable onRunAction;
    private Runnable onStopAction;
    private boolean isRunning = false;

    public ToggleButton() {
        super("▶ RUN");
        getStyleClass().add("run-button");

        setOnAction(e -> {
            if (isRunning) {
                stop();
            } else {
                run();
            }
        });
    }

    public void setRunAction(Runnable action) {
        this.onRunAction = action;
    }

    public void setStopAction(Runnable action) {
        this.onStopAction = action;
    }

    public void run() {
        if (isRunning) return;
        isRunning = true;
        setText("⏹ STOP");
        getStyleClass().remove("run-button");
        getStyleClass().add("stop-button");
        if (onRunAction != null) onRunAction.run();
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;
        setText("▶ RUN");
        getStyleClass().remove("stop-button");
        getStyleClass().add("run-button");
        if (onStopAction != null) onStopAction.run();
    }

    public void reset() {
        isRunning = false;
        setText("▶ RUN");
        getStyleClass().remove("stop-button");
        getStyleClass().add("run-button");
    }

    public boolean isRunning() {
        return isRunning;
    }
}
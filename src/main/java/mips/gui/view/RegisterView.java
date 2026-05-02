package mips.gui.view;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import mips.core.CpuListener;
import mips.core.ParsedCommand;
import mips.core.RegisterAliases;

import java.util.concurrent.ConcurrentHashMap;

public class RegisterView implements CpuListener {
    private final GridPane leftGrid;
    private final GridPane rightGrid;
    private final Label[][] leftLabels;  // [16][2] - 0 имя, 1 значение
    private final Label[][] rightLabels; // [16][2] - 0 имя, 1 значение
    private final VBox rootLayout;

    // Для подсветки измененных регистров
    private final ConcurrentHashMap<Integer, Timeline> highlightTimelines = new ConcurrentHashMap<>();
    private int[] lastValues = new int[32];

    public RegisterView() {
        this.leftGrid = new GridPane();
        this.rightGrid = new GridPane();
        this.leftLabels = new Label[16][2];
        this.rightLabels = new Label[16][2];

        // Инициализируем последние значения нулями
        for (int i = 0; i < 32; i++) {
            lastValues[i] = 0;
        }

        // Настройка сеток
        leftGrid.setVgap(0);
        leftGrid.setHgap(0);
        leftGrid.setPadding(Insets.EMPTY);
        leftGrid.getStyleClass().add("register-grid");

        rightGrid.setVgap(0);
        rightGrid.setHgap(0);
        rightGrid.setPadding(Insets.EMPTY);
        rightGrid.getStyleClass().add("register-grid");

        // ========== ЗАГОЛОВКИ ==========
        Label leftNameHeader = new Label("Register");
        leftNameHeader.getStyleClass().add("register-header");
        leftNameHeader.setMaxWidth(Double.MAX_VALUE);
        leftNameHeader.setAlignment(Pos.CENTER_LEFT);

        Label leftValueHeader = new Label("Value");
        leftValueHeader.getStyleClass().add("register-header");
        leftValueHeader.setMaxWidth(Double.MAX_VALUE);
        leftValueHeader.setAlignment(Pos.CENTER_RIGHT);

        Label rightNameHeader = new Label("Register");
        rightNameHeader.getStyleClass().add("register-header");
        rightNameHeader.setMaxWidth(Double.MAX_VALUE);
        rightNameHeader.setAlignment(Pos.CENTER_LEFT);

        Label rightValueHeader = new Label("Value");
        rightValueHeader.getStyleClass().add("register-header");
        rightValueHeader.setMaxWidth(Double.MAX_VALUE);
        rightValueHeader.setAlignment(Pos.CENTER_RIGHT);

        leftGrid.add(leftNameHeader, 0, 0);
        leftGrid.add(leftValueHeader, 1, 0);
        rightGrid.add(rightNameHeader, 0, 0);
        rightGrid.add(rightValueHeader, 1, 0);

        // ========== ЗАПОЛНЕНИЕ ==========
        for (int i = 0; i < 16; i++) {
            int row = i + 1;
            String rowClass = (i % 2 == 0) ? "row-even" : "row-odd";

            // Левая таблица
            String name = RegisterAliases.toName(i);
            leftLabels[i][0] = createCell(name, false, rowClass);
            leftLabels[i][1] = createCell("0", true, rowClass);
            leftGrid.add(leftLabels[i][0], 0, row);
            leftGrid.add(leftLabels[i][1], 1, row);

            // Правая таблица
            int regNum = i + 16;
            String rightName = RegisterAliases.toName(regNum);
            rightLabels[i][0] = createCell(rightName, false, rowClass);
            rightLabels[i][1] = createCell("0", true, rowClass);
            rightGrid.add(rightLabels[i][0], 0, row);
            rightGrid.add(rightLabels[i][1], 1, row);
        }

        // ========== ШИРИНА КОЛОНОК ==========
        ColumnConstraints nameCol = new ColumnConstraints(80, 80, 100);
        ColumnConstraints valueCol = new ColumnConstraints(90, 90, 150);
        valueCol.setHgrow(Priority.ALWAYS);

        leftGrid.getColumnConstraints().addAll(nameCol, valueCol);
        rightGrid.getColumnConstraints().addAll(nameCol, valueCol);

        // ========== РАМКИ ==========
        leftGrid.getStyleClass().add("register-grid-bordered");
        rightGrid.getStyleClass().add("register-grid-bordered");

        // ========== КОМПОНОВКА ==========
        HBox tablesContainer = new HBox(15, leftGrid, rightGrid);
        tablesContainer.setFillHeight(true);
        tablesContainer.getStyleClass().add("tables-container");
        HBox.setHgrow(leftGrid, Priority.ALWAYS);
        HBox.setHgrow(rightGrid, Priority.ALWAYS);

        Label title = new Label("REGISTERS");
        title.getStyleClass().add("register-title");

        this.rootLayout = new VBox(5, title, tablesContainer);
        this.rootLayout.setPadding(new Insets(10));
        this.rootLayout.getStyleClass().add("register-view");
        VBox.setVgrow(tablesContainer, Priority.ALWAYS);
    }

    private Label createCell(String text, boolean alignRight, String rowClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("register-cell", rowClass);
        if (alignRight) {
            label.getStyleClass().add("register-cell-value");
            label.setAlignment(Pos.CENTER_RIGHT);
        } else {
            label.getStyleClass().add("register-cell-name");
            label.setAlignment(Pos.CENTER_LEFT);
        }
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private void updateUI(int[] regValues) {
        for (int i = 0; i < 16; i++) {
            int newValue = regValues[i];
            int oldValue = lastValues[i];

            leftLabels[i][1].setText(String.valueOf(newValue));

            // Подсветка при изменении
            if (newValue != oldValue) {
                highlightRegister(i, true);
            }
        }

        for (int i = 0; i < 16; i++) {
            int regNum = i + 16;
            int newValue = regValues[regNum];
            int oldValue = lastValues[regNum];

            rightLabels[i][1].setText(String.valueOf(newValue));

            // Подсветка при изменении
            if (newValue != oldValue) {
                highlightRegister(regNum, false);
            }
        }

        // Сохраняем текущие значения для следующего сравнения
        System.arraycopy(regValues, 0, lastValues, 0, 32);
    }

    private void highlightRegister(int regIndex, boolean isLeft) {
        Label valueLabel;
        if (isLeft) {
            valueLabel = leftLabels[regIndex][1];
        } else {
            int rightIndex = regIndex - 16;
            if (rightIndex >= 0 && rightIndex < 16) {
                valueLabel = rightLabels[rightIndex][1];
            } else {
                return;
            }
        }

        // Добавляем класс подсветки
        valueLabel.getStyleClass().add("register-cell-highlight");

        // Удаляем старый таймер если есть
        Timeline oldTimeline = highlightTimelines.get(regIndex);
        if (oldTimeline != null) {
            oldTimeline.stop();
        }

        // Создаём новый таймер для сброса подсветки
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(300), e -> {
                    valueLabel.getStyleClass().remove("register-cell-highlight");
                    highlightTimelines.remove(regIndex);
                })
        );
        timeline.setCycleCount(1);
        timeline.play();
        highlightTimelines.put(regIndex, timeline);
    }

    // ========== Реализация CpuListener ==========

    @Override
    public void onRegistersChanged(int[] allRegisters) {
        Platform.runLater(() -> updateUI(allRegisters));
    }

    @Override
    public void onPcChanged(int pc) {
        // не используется
    }

    @Override
    public void onInstructionExecuted(ParsedCommand command) {
        // не используется
    }

    @Override
    public void onFetch(int pc, ParsedCommand command) {
        // не используется
    }

    @Override
    public void onDecode(ParsedCommand command) {
        // не используется
    }

    @Override
    public void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation) {
        // не используется
    }

    @Override
    public void onMemoryAccess(int address, int value, boolean isRead) {
        // не используется
    }

    @Override
    public void onWriteBack(int registerIndex, int value) {
        // не используется
    }

    @Override
    public void onHalted() {
        // не используется
    }

    public Node getView() {
        return rootLayout;
    }
}
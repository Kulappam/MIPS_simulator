package mips.gui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import mips.core.RegisterAliases;

public class RegisterView {
    private final GridPane leftGrid;
    private final GridPane rightGrid;
    private final Label[][] leftLabels;
    private final Label[][] rightLabels;
    private final VBox rootLayout;

    public RegisterView() {
        this.leftGrid = new GridPane();
        this.rightGrid = new GridPane();
        this.leftLabels = new Label[16][2];
        this.rightLabels = new Label[16][2];

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

    public void update(int[] regValues) {
        for (int i = 0; i < 16; i++) {
            leftLabels[i][1].setText(String.valueOf(regValues[i]));
        }
        for (int i = 0; i < 16; i++) {
            int regNum = i + 16;
            rightLabels[i][1].setText(String.valueOf(regValues[regNum]));
        }
    }

    public Node getView() {
        return rootLayout;
    }
}
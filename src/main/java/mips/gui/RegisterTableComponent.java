package mips.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RegisterTableComponent {
    private final TableView<RegisterData> leftTable;
    private final TableView<RegisterData> rightTable;
    private final VBox rootLayout;

    public RegisterTableComponent() {
        /*
        * Для вместимости всех регистров в окне приложения без ползунка прокручивания
        * было решено разделить таблицу регистров на две таблицы - левую и правую
        * левая таблица показывает регистры в диапазоне ($0-$15)
        * правая таблица показывает регистры в диапазоне ($16-$31)
         */
        this.leftTable = createTable();
        this.rightTable = createTable();

        // Инициализация данных
        initData();

        HBox tablesContainer = new HBox(5, leftTable, rightTable);
        HBox.setHgrow(leftTable, Priority.ALWAYS);
        HBox.setHgrow(rightTable, Priority.ALWAYS);

        this.rootLayout = new VBox(5, new Label("REGISTERS"), tablesContainer);
        this.rootLayout.setPadding(new Insets(10));
        VBox.setVgrow(tablesContainer, Priority.ALWAYS);
    }

    private void initData() {
        ObservableList<RegisterData> leftData = FXCollections.observableArrayList();
        ObservableList<RegisterData> rightData = FXCollections.observableArrayList();

        for (int i = 0; i < 32; i++) {
            RegisterData row = new RegisterData("$" + i, "0");
            if (i < 16) leftData.add(row);
            else rightData.add(row);
        }
        leftTable.setItems(leftData);
        rightTable.setItems(rightData);
    }

    private TableView<RegisterData> createTable() {
        TableView<RegisterData> table = new TableView<>();

        TableColumn<RegisterData, String> nameCol = new TableColumn<>("Reg");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        nameCol.setSortable(false);

        TableColumn<RegisterData, String> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(d -> d.getValue().valueProperty());
        valCol.setSortable(false);

        table.getColumns().addAll(nameCol, valCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }


    public void update(int[] regValues) {
        ObservableList<RegisterData> leftItems = leftTable.getItems();
        ObservableList<RegisterData> rightItems = rightTable.getItems();

        for (int i = 0; i < 16; i++) {
            leftItems.get(i).valueProperty().set(String.valueOf(regValues[i]));
        }
        for (int i = 16; i < 32; i++) {
            rightItems.get(i - 16).valueProperty().set(String.valueOf(regValues[i]));
        }
    }

    // Возвращение узла для нормальной работы с javafx
    public Node getView() {
        return rootLayout;
    }
}
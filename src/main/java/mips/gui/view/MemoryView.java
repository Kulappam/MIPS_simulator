package mips.gui.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryView {
    private final TableView<MemoryEntry> table;
    private final ObservableList<MemoryEntry> data;
    private final Map<Integer, MemoryEntry> addressToEntry;

    public MemoryView() {
        this.table = new TableView<>();
        this.data = FXCollections.observableArrayList();
        this.addressToEntry = new ConcurrentHashMap<>();

        // Колонка адреса
        TableColumn<MemoryEntry, String> addrCol = new TableColumn<>("Address");
        addrCol.setCellValueFactory(cell -> cell.getValue().addressProperty());
        addrCol.setPrefWidth(100);
        addrCol.setStyle("-fx-alignment: CENTER-LEFT;");

        // Колонка значения (HEX)
        TableColumn<MemoryEntry, String> hexCol = new TableColumn<>("Value (HEX)");
        hexCol.setCellValueFactory(cell -> cell.getValue().hexValueProperty());
        hexCol.setPrefWidth(120);
        hexCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Колонка значения (DEC)
        TableColumn<MemoryEntry, String> decCol = new TableColumn<>("Value (DEC)");
        decCol.setCellValueFactory(cell -> cell.getValue().decValueProperty());
        decCol.setPrefWidth(120);
        decCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        addrCol.setSortable(false);
        hexCol.setSortable(false);
        decCol.setSortable(false);

        table.getColumns().addAll(addrCol, hexCol, decCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Шахматная заливка строк
        table.setRowFactory(tv -> new TableRow<MemoryEntry>() {
            @Override
            protected void updateItem(MemoryEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    setStyle(index % 2 == 0 ? "-fx-background-color: #ffffff;" : "-fx-background-color: #f2f2f2;");
                }
            }
        });

        // Стиль заголовков как в RegisterView
        table.setStyle("-fx-border-color: #aaa; -fx-border-width: 1px;");
    }

    public void updateMemory(Map<Integer, Byte> memory, int dataStart) {
        // Очищаем старые записи
        data.clear();
        addressToEntry.clear();

        // Собираем слова (4 байта) из памяти
        Map<Integer, Integer> words = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, Byte> entry : memory.entrySet()) {
            int addr = entry.getKey();
            if (addr >= dataStart) {
                int wordAddr = addr & ~3;  // выравнивание до границы слова
                int byteOffset = addr % 4;
                int oldValue = words.getOrDefault(wordAddr, 0);
                int newValue = oldValue | ((entry.getValue() & 0xFF) << (24 - byteOffset * 8));
                words.put(wordAddr, newValue);
            }
        }

        // Сортируем по адресу и добавляем в таблицу
        words.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    MemoryEntry me = new MemoryEntry(entry.getKey(), entry.getValue());
                    data.add(me);
                    addressToEntry.put(entry.getKey(), me);
                });
    }

    public void updateSingleAddress(int address, int value) {
        // Удаляем старую запись и добавляем новую
        MemoryEntry existing = addressToEntry.remove(address);
        if (existing != null) {
            data.remove(existing);
        }
        MemoryEntry newEntry = new MemoryEntry(address, value);
        data.add(newEntry);
        addressToEntry.put(address, newEntry);
        // Сортируем
        data.sort((a, b) -> Integer.compare(a.getAddress(), b.getAddress()));
    }

    public Node getView() {
        VBox root = new VBox(5);
        Label title = new Label("DATA MEMORY");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        root.getChildren().addAll(title, table);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    // Класс для строки таблицы
    public static class MemoryEntry {
        private final SimpleStringProperty address;
        private final SimpleStringProperty hexValue;
        private final SimpleStringProperty decValue;
        private final int addr;

        public MemoryEntry(int address, int value) {
            this.addr = address;
            this.address = new SimpleStringProperty(String.format("0x%08X", address));
            this.hexValue = new SimpleStringProperty(String.format("0x%08X", value));
            this.decValue = new SimpleStringProperty(String.valueOf(value));
        }

        public int getAddress() { return addr; }
        public SimpleStringProperty addressProperty() { return address; }
        public SimpleStringProperty hexValueProperty() { return hexValue; }
        public SimpleStringProperty decValueProperty() { return decValue; }
    }
}
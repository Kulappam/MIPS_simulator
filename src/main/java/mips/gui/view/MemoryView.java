package mips.gui.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mips.core.Memory;
import mips.core.MemoryListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryView implements MemoryListener {
    private final TableView<MemoryEntry> table;
    private final ObservableList<MemoryEntry> data;
    private final Map<Integer, MemoryEntry> addressToEntry;
    private int dataStart;

    private static final int MAX_DISPLAY_WORDS = 512; // 2KB памяти

    // Таймер для сброса подсветки
    private final Map<Integer, javafx.animation.Timeline> highlightTimelines = new ConcurrentHashMap<>();

    public MemoryView() {
        this.table = new TableView<>();
        this.data = FXCollections.observableArrayList();
        this.addressToEntry = new ConcurrentHashMap<>();
        this.dataStart = Memory.DATA_START;

        // Колонка адреса
        TableColumn<MemoryEntry, String> addrCol = new TableColumn<>("Address");
        addrCol.setCellValueFactory(cell -> cell.getValue().addressProperty());
        addrCol.setPrefWidth(110);
        addrCol.setSortable(false);
        addrCol.getStyleClass().add("memory-addr-column");

        // Колонка значения (HEX)
        TableColumn<MemoryEntry, String> hexCol = new TableColumn<>("Value (HEX)");
        hexCol.setCellValueFactory(cell -> cell.getValue().hexValueProperty());
        hexCol.setPrefWidth(130);
        hexCol.setSortable(false);
        hexCol.getStyleClass().add("memory-value-column");

        // Колонка значения (DEC)
        TableColumn<MemoryEntry, String> decCol = new TableColumn<>("Value (DEC)");
        decCol.setCellValueFactory(cell -> cell.getValue().decValueProperty());
        decCol.setPrefWidth(130);
        decCol.setSortable(false);
        decCol.getStyleClass().add("memory-value-column");

        table.getColumns().addAll(addrCol, hexCol, decCol);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Шахматная заливка строк
        table.setRowFactory(tv -> new TableRow<MemoryEntry>() {
            @Override
            protected void updateItem(MemoryEntry item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("memory-row-even", "memory-row-odd", "memory-row-highlight");

                if (empty || item == null) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    getStyleClass().add(index % 2 == 0 ? "memory-row-even" : "memory-row-odd");

                    // Если ячейка была недавно изменена, добавляем класс подсветки
                    if (item.isHighlighted()) {
                        getStyleClass().add("memory-row-highlight");
                    }
                }
            }
        });

        table.getStyleClass().add("memory-table");
    }

    // ========== MemoryListener ==========

    @Override
    public void onMemoryChanged(int address, int value) {
        Platform.runLater(() -> {
            // Выравниваем адрес до границы слова
            int wordAddr = address & ~3;

            MemoryEntry entry = addressToEntry.get(wordAddr);
            if (entry != null) {
                // Старое значение
                int oldValue = entry.getValue();

                // Обновляем значение
                entry.setValue(value);

                // Подсвечиваем строку
                highlightRow(wordAddr);

                // Прокручиваем к строке
                scrollToAddress(wordAddr);
            } else {
                // Если записи нет — обновляем всю таблицу
                rebuildTable();
            }
        });
    }

    @Override
    public void onMemoryReset() {
        Platform.runLater(this::rebuildTable);
    }

    // ========== Публичные методы ==========

    public void setDataStart(int dataStart) {
        this.dataStart = dataStart;
        rebuildTable();
    }

    public void rebuildTable() {
        data.clear();
        addressToEntry.clear();
        highlightTimelines.clear();

        int endAddr = Math.min(dataStart + MAX_DISPLAY_WORDS * 4, Memory.DATA_END);
        for (int addr = dataStart; addr < endAddr; addr += 4) {
            MemoryEntry entry = new MemoryEntry(addr, 0);
            data.add(entry);
            addressToEntry.put(addr, entry);
        }
    }

    public void highlightRow(int address) {
        int wordAddr = address & ~3;
        MemoryEntry entry = addressToEntry.get(wordAddr);
        if (entry == null) return;

        entry.setHighlighted(true);
        table.refresh();

        // Сбрасываем подсветку через 500 мс
        javafx.animation.Timeline timeline = highlightTimelines.get(wordAddr);
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(500), e -> {
                    entry.setHighlighted(false);
                    table.refresh();
                    highlightTimelines.remove(wordAddr);
                })
        );
        highlightTimelines.put(wordAddr, timeline);
        timeline.play();
    }

    public void scrollToAddress(int address) {
        int wordAddr = address & ~3;
        MemoryEntry entry = addressToEntry.get(wordAddr);
        if (entry == null) return;

        int index = data.indexOf(entry);
        if (index >= 0) {
            table.scrollTo(index);
        }
    }

    public Node getView() {
        VBox root = new VBox(5);
        Label title = new Label("DATA MEMORY");
        title.getStyleClass().add("memory-title");
        root.getChildren().addAll(title, table);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getStyleClass().add("memory-view");
        return root;
    }

    // ========== Внутренний класс для строки таблицы ==========

    public static class MemoryEntry {
        private final SimpleStringProperty address;
        private final SimpleObjectProperty<Integer> value;
        private final int addr;
        private volatile boolean highlighted = false;

        public MemoryEntry(int address, int value) {
            this.addr = address;
            this.address = new SimpleStringProperty(String.format("0x%08X", address));
            this.value = new SimpleObjectProperty<>(value);
        }

        public int getAddress() { return addr; }
        public int getValue() { return value.get(); }
        public void setValue(int newValue) { value.set(newValue); }
        public boolean isHighlighted() { return highlighted; }
        public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }

        public SimpleStringProperty addressProperty() { return address; }
        public SimpleObjectProperty<Integer> valueProperty() { return value; }
        public SimpleStringProperty hexValueProperty() {
            return new SimpleStringProperty(String.format("0x%08X", value.get()));
        }
        public SimpleStringProperty decValueProperty() {
            return new SimpleStringProperty(String.valueOf(value.get()));
        }
    }
}
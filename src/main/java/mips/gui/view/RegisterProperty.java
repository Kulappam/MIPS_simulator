package mips.gui.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Благодаря properties из javafx, могу реализовать динамическое обновление
// отображения данных в интерфейсе приложения
// Но поскольку TableView не умеет напрямую в динамическое обновление,
// то я сделал отдельный класс RegisterProperty

public class RegisterProperty {
    private final StringProperty name;
    private final StringProperty value;

    public RegisterProperty(String name, String value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty valueProperty() { return value; }
}
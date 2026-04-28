package mips.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RegisterData {
    private final StringProperty name;
    private final StringProperty value;

    public RegisterData(String name, String value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty valueProperty() { return value; }
}
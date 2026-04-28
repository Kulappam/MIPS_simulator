package mips.gui;

import javafx.scene.Node;
/**
 * Интерфейс-крючок для редактора кода
 * Нужен чтобы реализовать независимый от реализации интерфейс общения
 * между логикой и визуалом программы
 */
public interface CodeEditor {
    String getText();
    void setText(String text);

    void highLightLine(int lineIndex);

    void clearHighLights();

    Node getView();
}

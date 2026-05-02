package mips.gui.view;

import javafx.scene.Node;
/**
 * Интерфейс-крючок
 * Позволяет отделить реализацию вьюшки от её интерфейса,
 * чтобы в будущем реализовать RichEditorView,
 * но пока можем ограничиться SimpleEditorView
 * TODO реализовать RichEditorView
 */
public interface EditorView {
    String getText();
    void setText(String text);

    void highLightLine(int lineIndex);

    void clearHighLights();

    Node getView();
}

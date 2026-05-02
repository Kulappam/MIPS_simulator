package mips.gui.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;


// максимально простая реализация интерфейса на простом TextArea
// TODO заменить на RichCodeEditor для защиты диплома

public class SimpleEditorView implements EditorView {
    private final TextArea textArea;

    public SimpleEditorView(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void setText(String text) {
        textArea.setText(text);
    }

    @Override
    public void highLightLine(int lineIndex) {
        //placeholder
        System.out.println("GUI: highlight string " + (lineIndex + 1));
    }

    @Override
    public void clearHighLights() {
        System.out.println("GUI: removed highlights");
    }

    @Override
    public Node getView() {
        return this.textArea;
    }
}

package mips.gui.view;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.time.Duration;

/**
 * Редактор кода с подсветкой синтаксиса.
 * Использует RichTextFX для нормальной работы.
 */
public class EditorView {
    private final StyleClassedTextArea codeArea;
    private final StackPane root;

    public EditorView() {
        this.codeArea = new StyleClassedTextArea();
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        this.codeArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");

        // Подсветка синтаксиса при изменении текста
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> highlightSyntax());

        codeArea.setFocusTraversable(true);

        this.root = new StackPane(codeArea);
        this.root.setFocusTraversable(false);
        this.root.setOnMouseClicked(e -> codeArea.requestFocus());
        this.root.setStyle("-fx-background-color: white;");
    }

    private void highlightSyntax() {
        String text = codeArea.getText();
        codeArea.clearStyle(0, text.length());

        // Простейшая подсветка: инструкции MIPS
        String[] keywords = {
                "li", "add", "sub", "lw", "sw", "beq", "bne", "j", "jal", "jr",
                "syscall", "move", "mfhi", "mflo", "mult", "div"
        };

        String[] registers = {
                "\\$zero", "\\$at", "\\$v0", "\\$v1", "\\$a0", "\\$a1", "\\$a2", "\\$a3",
                "\\$t0", "\\$t1", "\\$t2", "\\$t3", "\\$t4", "\\$t5", "\\$t6", "\\$t7",
                "\\$s0", "\\$s1", "\\$s2", "\\$s3", "\\$s4", "\\$s5", "\\$s6", "\\$s7",
                "\\$t8", "\\$t9", "\\$k0", "\\$k1", "\\$gp", "\\$sp", "\\$fp", "\\$ra"
        };

        // Подсветка ключевых слов
        for (String kw : keywords) {
            highlightPattern("\\b" + kw + "\\b", "keyword");
        }

        // Подсветка регистров
        for (String reg : registers) {
            highlightPattern(reg, "register");
        }

        // Подсветка чисел
        highlightPattern("\\b\\d+\\b", "number");
        highlightPattern("0x[0-9a-fA-F]+", "number");

        // Подсветка комментариев
        highlightPattern("#.*$", "comment");
    }

    private void highlightPattern(String regex, String styleClass) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(codeArea.getText());
        while (matcher.find()) {
            codeArea.setStyleClass(matcher.start(), matcher.end(), styleClass);
        }
    }

    public void setText(String text) {
        codeArea.replaceText(text);
        highlightSyntax();
    }

    public String getText() {
        return codeArea.getText();
    }

    public void clear() {
        codeArea.clear();
    }

    public void highlightLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= codeArea.getParagraphs().size()) return;

        int start = codeArea.getAbsolutePosition(lineIndex, 0);
        int end = codeArea.getAbsolutePosition(lineIndex, codeArea.getParagraphs().get(lineIndex).length() - 1);

        codeArea.setStyleClass(start, end, "highlighted-line");

        // Сброс подсветки через 500ms
        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(500),
                        e -> codeArea.setStyleClass(start, end, "")
                )
        ).play();

        // Прокрутка к строке
        codeArea.showParagraphAtTop(lineIndex);
    }

    public Node getView() {
        return root;
    }
}
package mips.gui.view;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import mips.core.CpuListener;
import mips.core.ParsedCommand;

import java.util.HashMap;
import java.util.Map;

public class CanvasDataPathView implements CpuListener, DataPath {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 300;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Map<String, Color> activeBlocks = new HashMap<>();
    private final Map<Arrow, Color> activeArrows = new HashMap<>();

    // Блоки
    private static final Block PC = new Block(30, 120, 60, 40);
    private static final Block IMEM = new Block(130, 120, 100, 40);
    private static final Block REGFILE = new Block(280, 110, 110, 60);
    private static final Block ALU = new Block(450, 120, 80, 40);
    private static final Block DMEM = new Block(580, 120, 100, 40);

    // Стрелки
    private static final Arrow ARROW_PC_IMEM = new Arrow(PC.x + PC.w, PC.y + PC.h/2, IMEM.x, IMEM.y + IMEM.h/2);
    private static final Arrow ARROW_IMEM_REGFILE = new Arrow(IMEM.x + IMEM.w, IMEM.y + IMEM.h/2, REGFILE.x, REGFILE.y + REGFILE.h/2);
    private static final Arrow ARROW_REGFILE_ALU = new Arrow(REGFILE.x + REGFILE.w, REGFILE.y + REGFILE.h/2, ALU.x, ALU.y + ALU.h/2);
    private static final Arrow ARROW_ALU_DMEM = new Arrow(ALU.x + ALU.w, ALU.y + ALU.h/2, DMEM.x, DMEM.y + DMEM.h/2);

    private static final Color NORMAL_BLOCK = Color.LIGHTGRAY;
    private static final Color ACTIVE_BLOCK = Color.GOLD;
    private static final Color NORMAL_ARROW = Color.BLACK;
    private static final Color ACTIVE_ARROW = Color.RED;

    public CanvasDataPathView() {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        drawStatic();
    }

    private void drawStatic() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));

        drawBlock(PC, "PC");
        drawBlock(IMEM, "IMEM");
        drawBlock(REGFILE, "RegFile");
        drawBlock(ALU, "ALU");
        drawBlock(DMEM, "DMEM");

        drawArrow(ARROW_PC_IMEM);
        drawArrow(ARROW_IMEM_REGFILE);
        drawArrow(ARROW_REGFILE_ALU);
        drawArrow(ARROW_ALU_DMEM);
    }

    private void drawBlock(Block b, String label) {
        Color fill = activeBlocks.getOrDefault(label, NORMAL_BLOCK);
        gc.setFill(fill);
        gc.fillRect(b.x, b.y, b.w, b.h);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(b.x, b.y, b.w, b.h);
        gc.setFill(Color.BLACK);
        gc.fillText(label, b.x + b.w/2 - 15, b.y + b.h/2 + 5);
    }

    private void drawArrow(Arrow a) {
        Color color = activeArrows.getOrDefault(a, NORMAL_ARROW);
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeLine(a.startX, a.startY, a.endX, a.endY);
        // Наконечник
        double angle = Math.atan2(a.endY - a.startY, a.endX - a.startX);
        double arrowSize = 8;
        double x1 = a.endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double y1 = a.endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double x2 = a.endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double y2 = a.endY - arrowSize * Math.sin(angle + Math.PI / 6);
        gc.strokeLine(a.endX, a.endY, x1, y1);
        gc.strokeLine(a.endX, a.endY, x2, y2);
    }

    private void highlight(String block, Arrow... arrows) {
        activeBlocks.clear();
        activeArrows.clear();
        if (block != null) activeBlocks.put(block, ACTIVE_BLOCK);
        for (Arrow a : arrows) activeArrows.put(a, ACTIVE_ARROW);
        drawStatic();
    }

    // CpuListener
    @Override
    public void onFetch(int pc, ParsedCommand command) {
        Platform.runLater(() -> highlight("PC", ARROW_PC_IMEM));
    }

    @Override
    public void onDecode(ParsedCommand command) {
        Platform.runLater(() -> highlight("RegFile", ARROW_IMEM_REGFILE));
    }

    @Override
    public void onExecute(int op1, int op2, int res, String operation) {
        Platform.runLater(() -> highlight("ALU", ARROW_REGFILE_ALU));
    }

    @Override
    public void onMemoryAccess(int address, int value, boolean isRead) {
        Platform.runLater(() -> highlight("DMEM", ARROW_ALU_DMEM));
    }

    @Override
    public void onWriteBack(int registerIndex, int value) {
        Platform.runLater(() -> highlight("RegFile")); // стрелки пока нет для обратной записи
    }

    @Override
    public void onHalted() {
        Platform.runLater(() -> {
            activeBlocks.clear();
            activeArrows.clear();
            drawStatic();
        });
    }

    // Пустые методы
    @Override public void onRegistersChanged(int[] regs) {}
    @Override public void onPcChanged(int pc) {}
    @Override public void onInstructionExecuted(ParsedCommand cmd) {}

    public Node getView() {
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return scroll;
    }

    private static class Block {
        double x, y, w, h;
        Block(double x, double y, double w, double h) { this.x = x; this.y = y; this.w = w; this.h = h; }
    }

    private static class Arrow {
        double startX, startY, endX, endY;
        Arrow(double sx, double sy, double ex, double ey) { startX = sx; startY = sy; endX = ex; endY = ey; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Arrow)) return false;
            Arrow a = (Arrow) o;
            return Double.compare(a.startX, startX) == 0 && Double.compare(a.startY, startY) == 0 &&
                    Double.compare(a.endX, endX) == 0 && Double.compare(a.endY, endY) == 0;
        }
        @Override
        public int hashCode() {
            int result = Double.hashCode(startX);
            result = 31 * result + Double.hashCode(startY);
            result = 31 * result + Double.hashCode(endX);
            result = 31 * result + Double.hashCode(endY);
            return result;
        }
    }
}
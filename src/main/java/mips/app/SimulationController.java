package mips.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

import mips.core.*;
import mips.gui.MainWindow;
import mips.exceptions.InvalidInstructionException;



public class SimulationController implements CpuListener, MemoryListener {

    private final Cpu cpu;
    private final Parser parser;
    private final MainWindow window;  // может быть null до setWindow()

    private Thread runThread;
    private volatile boolean running = false;
    private int stepDelayMs = 100;

    public enum SimulatorState {
        IDLE,       // Не выполняется, готов к запуску
        RUNNING,    // Выполняется (RUN или STEP)
        HALTED      // Программа завершена (syscall 10 или ошибка)
    }

    public interface StateListener {
        void onStateChanged(SimulatorState newState);
    }

    private SimulatorState state = SimulatorState.IDLE;
    private final List<StateListener> stateListeners = new ArrayList<>();

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    private void setState(SimulatorState newState) {
        if (this.state != newState) {
            this.state = newState;
            for (StateListener l : stateListeners) {
                Platform.runLater(() -> l.onStateChanged(newState));
            }
        }
    }

    public SimulatorState getState() {
        return state;
    }

    // Полноценный конструктор (рекомендую использовать)
    public SimulationController(Cpu cpu, Parser parser, MainWindow window) {
        this.cpu = cpu;
        this.parser = parser;
        this.window = window;

        if (cpu != null) {
            cpu.addListener(this);
            cpu.getMemory().addListener(this);
        }
    }

    public Map<Integer, Byte> getMemoryBytes() {
        return cpu.getMemory().getBytes();
    }

    public Boolean isRunning() {
        return running;
    }

    // ==================== КОМАНДЫ ОТ GUI ====================

    public void onStep() {
        if (cpu == null) return;
        try {
            if (!isProgramLoaded()) {
                loadProgramFromEditor();
            }
            cpu.step();
        } catch (RuntimeException e) {
            ErrorHandler.reportError(e);
        }
    }

    public void onRun() {
        if (state == SimulatorState.HALTED) {
            onReset();
        }
        if (!isProgramLoaded()) {
            try {
                loadProgramFromEditor();
            } catch (RuntimeException e) {
                ErrorHandler.reportError(e);
                return;
            }
        }
        if (runThread != null && runThread.isAlive()) return;

        setState(SimulatorState.RUNNING);

        running = true;
        runThread = new Thread(() -> {
            try {
                while (running && cpu.step()) {
                    if (stepDelayMs > 0) Thread.sleep(stepDelayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                Platform.runLater(() -> ErrorHandler.reportError(e));
                setState(SimulatorState.HALTED);
                running = false;
            } finally {
                if (running) {
                    setState(SimulatorState.IDLE);
                }
                running = false;
            }
        });
        runThread.setDaemon(true);
        runThread.start();
    }

    public void onStop() {
        running = false;
        if (runThread != null) {
            runThread.interrupt();
            runThread = null;
        }
        setState(SimulatorState.IDLE);
    }

    public void onReset() {
        cpu.reset();
        running = false;
        if (runThread != null && runThread.isAlive()) {
            runThread.interrupt();
            runThread = null;
        }
        setState(SimulatorState.IDLE);
    }

    public void setStepDelay(int delayMs) {
        this.stepDelayMs = Math.max(0, delayMs);
    }

    // ==================== РЕАЛИЗАЦИЯ CpuListener ====================

    @Override
    public void onRegistersChanged(int[] allRegisters) {
        if (window != null) {
            Platform.runLater(() -> window.updateRegisters(allRegisters));
        }
    }

    @Override
    public void onPcChanged(int pc) {
        if (window != null) {
            Platform.runLater(() -> window.highlightLine(pc));
        }
    }

    @Override
    public void onInstructionExecuted(ParsedCommand command) {
        if (window != null) {
            Platform.runLater(() -> window.onInstructionExecuted(command));
        }
    }

    @Override
    public void onFetch(int pc, ParsedCommand command) {
        if (window != null) {
            Platform.runLater(() -> window.onFetch(pc, command));
        }
    }

    @Override
    public void onDecode(ParsedCommand command) {
        if (window != null) {
            Platform.runLater(() -> window.onDecode(command));
        }
    }

    @Override
    public void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation) {
        if (window != null) {
            Platform.runLater(() -> window.onExecute(aluOperand1, aluOperand2, aluResult, operation));
        }
    }

    @Override
    public void onMemoryAccess(int address, int value, boolean isRead) {
        if (window != null) {
            Platform.runLater(() -> window.onMemoryAccess(address, value, isRead));
        }
    }

    @Override
    public void onWriteBack(int registerIndex, int value) {
        if (window != null) {
            Platform.runLater(() -> window.onWriteBack(registerIndex, value));
        }
    }

    @Override
    public void onHalted() {
        if (window != null) {
            Platform.runLater(() -> {
                window.consoleLog("Program halted");
                window.onSimulatorStateChanged(SimulatorState.HALTED);
            });
        }
        setState(SimulatorState.HALTED);
    }

    // ==================== РЕАЛИЗАЦИЯ MemoryListener ====================

    @Override
    public void onMemoryChanged(int address, int value) {
        if (window != null) {
            Platform.runLater(() -> window.updateMemoryCell(address, value));
        }
    }

    @Override
    public void onMemoryReset() {
        if (window != null) {
            Platform.runLater(() -> window.refreshMemoryDump());
        }
    }

    // ==================== ПРИВАТНЫЕ МЕТОДЫ ====================

    private boolean isProgramLoaded() {
        return cpu != null && cpu.getMemory().getInstructionCount() > 0;
    }

    private void loadProgramFromEditor() {
        if (cpu == null || parser == null || window == null) return;
        String sourceCode = window.getEditorText();
        List<ParsedCommand> program = parser.parseProgram(sourceCode);
        if (program.isEmpty()) {
            throw new InvalidInstructionException("Нет корректных инструкций в редакторе");
        }
        cpu.loadProgram(program);
    }
}
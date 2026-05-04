package mips.app;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import mips.core.Cpu;
import mips.exceptions.SyscallExitException;

public class SimulationController {

    private final Cpu cpu;

    private Thread runThread;
    private int stepDelayMs = 100;

    private volatile boolean programEnded = false;
    private volatile boolean programError = false;

    private ControllerState state = ControllerState.IDLE;
    private final List<StateListener> stateListeners = new ArrayList<>();

    public enum ControllerState {
        IDLE,       // ничего не выполняется
        RUNNING     // идёт циклическое выполнение
    }

    public interface StateListener {
        void onStateChanged(ControllerState newState);
    }

    public SimulationController(Cpu cpu) {
        this.cpu = cpu;
    }

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    private void setState(ControllerState newState) {
        if (this.state != newState) {
            this.state = newState;
            for (StateListener l : stateListeners) {
                Platform.runLater(() -> l.onStateChanged(newState));
            }
        }
    }

    public void step() {
        if (state == ControllerState.RUNNING) return;

        try {
            cpu.step();
        } catch (RuntimeException e) {
            ErrorHandler.reportError(e);
        }
    }


    public void run() {
        if (state == ControllerState.RUNNING) return;

        // Проверяем, нужно ли сбрасывать перед запуском
        boolean needsReset = !isProgramLoaded() || programEnded || programError;
        if (needsReset) {
            reset();
        }

        if (!isProgramLoaded()) {
            ErrorHandler.reportError(new IllegalStateException("CONTROLLER: No Program"));
            return;
        }

        setState(ControllerState.RUNNING);

        runThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    cpu.step();
                    if (stepDelayMs > 0) Thread.sleep(stepDelayMs);
                }
            } catch (SyscallExitException e) {
                Platform.runLater(() -> {
                    ErrorHandler.reportSyscall(e.getMessage());
                    programEnded = true;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    ErrorHandler.reportError(e);
                    programError = true;
                });
            } finally {
                setState(ControllerState.IDLE);
            }
        });
        runThread.setDaemon(true);
        runThread.start();
    }


    public void stop() {
        if (state != ControllerState.RUNNING) return;
        if (runThread != null) {
            runThread.interrupt();
            runThread = null;
        }
        setState(ControllerState.IDLE);
    }

    public void reset() {
        if (state == ControllerState.RUNNING) {
            stop();
        }
        cpu.reset();
        programEnded = false;
        programError = false;
    }

    private boolean isProgramLoaded() {
        return cpu.getMemory().getInstructionCount() > 0;
    }


    public void setStepDelay(int delayMs) {
        this.stepDelayMs = Math.max(0, delayMs);
    }
}
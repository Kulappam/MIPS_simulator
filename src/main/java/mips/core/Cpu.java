package mips.core;

import java.util.ArrayList;
import java.util.List;

import mips.exceptions.*;

/**
 * Процессор MIPS.
 * Реализует 5 стадий: Fetch → Decode → Execute → Memory → WriteBack
 */
public class Cpu {
    private final Memory memory;
    private final RegisterFile registers;
    private int pc;  // реальный адрес (не индекс!)

    private final List<CpuListener> listeners = new ArrayList<>();

    // Флаг остановки
    private boolean halted = false;

    public Cpu(Memory memory, RegisterFile registers) {
        this.memory = memory;
        this.registers = registers;
        this.pc = Memory.TEXT_START;
    }

    // ========== Управление ==========

    public void reset() {
        registers.reset();
        memory.reset();
        pc = Memory.TEXT_START;
        halted = false;
        notifyRegistersChanged();
        notifyPcChanged();
    }

    public void loadProgram(List<ParsedCommand> program) {
        memory.loadProgram(program, Memory.TEXT_START);
        pc = Memory.TEXT_START;
        halted = false;
        notifyPcChanged();
    }

    /**
     * Выполнить одну инструкцию
     * @return false если программа завершена (нет инструкции)
     */
    public boolean step() {
        if (halted) return false;

        try {
            // ===== STAGE 1: FETCH =====
            ParsedCommand cmd = memory.fetchInstruction(pc);
            notifyFetch(pc, cmd);

            // ===== STAGE 2: DECODE (неявный, просто уведомление) =====
            notifyDecode(cmd);

            // ===== STAGE 3-5: EXECUTE, MEMORY, WRITEBACK =====
            executeInstruction(cmd);

            // Обновляем PC (по умолчанию +4, ветвления могут изменить)
            pc += 4;
            notifyPcChanged();

            notifyInstructionExecuted(cmd);
            return true;

        } catch (RuntimeException e) {
            halted = true;
            throw e;
        }
    }

    /**
     * Исполнение конкретной инструкции с уведомлениями для DataPath
     */
    private void executeInstruction(ParsedCommand cmd) {
        InstructionType type = cmd.type();
        int[] regs = cmd.regIndices();
        int imm = cmd.immediate();

        switch (type) {
            case LI: // li $rd, imm
                registers.write(regs[0], imm);
                notifyWriteBack(regs[0], imm);
                notifyExecute(0, imm, imm, "LI");
                break;

            case ADD: // add $rd, $rs, $rt
                int rsAdd = registers.read(regs[1]);
                int rtAdd = registers.read(regs[2]);
                int resultAdd = rsAdd + rtAdd;
                notifyExecute(rsAdd, rtAdd, resultAdd, "ADD");
                registers.write(regs[0], resultAdd);
                notifyWriteBack(regs[0], resultAdd);
                break;

            case SUB: // sub $rd, $rs, $rt
                int rsSub = registers.read(regs[1]);
                int rtSub = registers.read(regs[2]);
                int resultSub = rsSub - rtSub;
                notifyExecute(rsSub, rtSub, resultSub, "SUB");
                registers.write(regs[0], resultSub);
                notifyWriteBack(regs[0], resultSub);
                break;

            case LW: // lw $rt, offset($rs)
                int baseLw = registers.read(regs[1]);
                int addressLw = baseLw + imm;
                notifyExecute(baseLw, imm, addressLw, "LW_ADDR");
                int valueLw = memory.readWord(addressLw);
                notifyMemoryAccess(addressLw, valueLw, true);
                registers.write(regs[0], valueLw);
                notifyWriteBack(regs[0], valueLw);
                break;

            case SW: // sw $rt, offset($rs)
                int baseSw = registers.read(regs[1]);
                int addressSw = baseSw + imm;
                int valueSw = registers.read(regs[0]);
                notifyExecute(baseSw, imm, addressSw, "SW_ADDR");
                memory.writeWord(addressSw, valueSw);
                notifyMemoryAccess(addressSw, valueSw, false);
                break;

            case SYSCALL:
                int service = registers.read(2);  // $v0 = регистр 2
                if (service == 10) {
                    halted = true;
                    notifyHalted();
                    return;
                }
                throw new UnsupportedOperationException("syscall " + service + " not implemented");

                // TODO: добить остальные инструкции (BEQ, BNE, J, JAL, SLT, AND, OR и тп)

            default:
                throw new InvalidInstructionException("Unknown instruction: " + type);
        }

        notifyRegistersChanged();
    }

    // ========== Геттеры для GUI (только чтение) ==========

    public int getPc() { return pc; }
    public RegisterFile getRegisters() { return registers; }
    public Memory getMemory() { return memory; }
    public boolean isHalted() { return halted; }

    // ========== Управление слушателями ==========

    public void addListener(CpuListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CpuListener listener) {
        listeners.remove(listener);
    }

    // ========== Уведомления ==========

    private void notifyRegistersChanged() {
        int[] all = registers.getAll();
        for (CpuListener l : listeners) {
            l.onRegistersChanged(all);
        }
    }

    private void notifyPcChanged() {
        for (CpuListener l : listeners) {
            l.onPcChanged(pc);
        }
    }

    private void notifyInstructionExecuted(ParsedCommand cmd) {
        for (CpuListener l : listeners) {
            l.onInstructionExecuted(cmd);
        }
    }

    private void notifyFetch(int pc, ParsedCommand cmd) {
        for (CpuListener l : listeners) {
            l.onFetch(pc, cmd);
        }
    }

    private void notifyDecode(ParsedCommand cmd) {
        for (CpuListener l : listeners) {
            l.onDecode(cmd);
        }
    }

    private void notifyExecute(int op1, int op2, int result, String operation) {
        for (CpuListener l : listeners) {
            l.onExecute(op1, op2, result, operation);
        }
    }

    private void notifyMemoryAccess(int address, int value, boolean isRead) {
        for (CpuListener l : listeners) {
            l.onMemoryAccess(address, value, isRead);
        }
    }

    private void notifyWriteBack(int regIndex, int value) {
        for (CpuListener l : listeners) {
            l.onWriteBack(regIndex, value);
        }
    }

    private void notifyHalted() {
        for (CpuListener l: listeners) {
            l.onHalted();
        }
    }
}
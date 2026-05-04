package mips.core;

import java.util.ArrayList;
import java.util.List;

import mips.app.ErrorHandler;
import mips.exceptions.*;

/**
 * Процессор MIPS.
 * Реализует 5 стадий: Fetch → Decode → Execute → Memory → WriteBack
 */
public class Cpu {
    private final Memory        memory;
    private final RegisterFile  registers;
    private int                 pc;

    private final List<CpuListener> listeners = new ArrayList<>();

    public Cpu(Memory memory, RegisterFile registers) {
        this.memory     = memory;
        this.registers  = registers;
        this.pc         = Memory.TEXT_START;
    }


    public void addListener(CpuListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CpuListener listener) {
        listeners.remove(listener);
    }


    public void reset() {
        registers.reset();
        pc = Memory.TEXT_START;
        notifyRegistersChanged();
        notifyPcChanged();
    }

    public void step() {
        ParsedCommand cmd = memory.fetchInstruction(pc);
        notifyFetch(pc, cmd);

        InstructionType type = cmd.type();
        int[] regs = cmd.regIndices();
        int imm = cmd.immediate();

        notifyDecode(cmd);

        switch (type) {
            case LI:
                registers.write(regs[0], imm);
                notifyWriteBack(regs[0], imm);
                notifyExecute(0, imm, imm, "LI");
                break;

            case ADD:
                int rsAdd = registers.read(regs[1]);
                int rtAdd = registers.read(regs[2]);
                int resultAdd = rsAdd + rtAdd;
                notifyExecute(rsAdd, rtAdd, resultAdd, "ADD");
                registers.write(regs[0], resultAdd);
                notifyWriteBack(regs[0], resultAdd);
                break;

            case SUB:
                int rsSub = registers.read(regs[1]);
                int rtSub = registers.read(regs[2]);
                int resultSub = rsSub - rtSub;
                notifyExecute(rsSub, rtSub, resultSub, "SUB");
                registers.write(regs[0], resultSub);
                notifyWriteBack(regs[0], resultSub);
                break;

            case LW:
                int baseLw = registers.read(regs[1]);
                int addressLw = baseLw + imm;
                notifyExecute(baseLw, imm, addressLw, "LW_ADDR");
                int valueLw = memory.readWord(addressLw);
                notifyMemoryAccess(addressLw, valueLw, true);
                registers.write(regs[0], valueLw);
                notifyWriteBack(regs[0], valueLw);
                break;

            case SW:
                int baseSw = registers.read(regs[1]);
                int addressSw = baseSw + imm;
                int valueSw = registers.read(regs[0]);
                notifyExecute(baseSw, imm, addressSw, "SW_ADDR");
                memory.writeWord(addressSw, valueSw);
                notifyMemoryAccess(addressSw, valueSw, false);
                break;

            case SYSCALL:
                int service = registers.read(2);
                switch (service) {
                    case 10:
                        notifyHalted();
                        ErrorHandler.reportSyscall("Program exited by syscall 10");
                        throw new SyscallExitException("CPU: Program Exited (syscall " + service + ")");
                    default:
                        throw new UnsupportedOperationException("CPU: syscall " + service + " Not Implemented");
                }

            default:
                throw new InvalidInstructionException("CPU: Unknown Instruction: " + type);
        }
        ErrorHandler.reportInfo("Instruction " + cmd.toString() + " was executed");
        pc += 4;
        notifyPcChanged();
        notifyRegistersChanged();
        notifyInstructionExecuted(cmd);
    }


    public Memory getMemory() {
        return memory;
    }


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
        for (CpuListener l : listeners) {
            l.onHalted();
        }
    }
}
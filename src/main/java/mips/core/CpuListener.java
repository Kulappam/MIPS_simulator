package mips.core;


public interface CpuListener {

    void onRegistersChanged(int[] allRegisters);

    void onPcChanged(int pc);

    void onInstructionExecuted(ParsedCommand command);

    void onFetch(int pc, ParsedCommand command);

    void onDecode(ParsedCommand command);

    void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation);

    void onMemoryAccess(int address, int value, boolean isRead);

    void onWriteBack(int registerIndex, int value);

    void onHalted();
}
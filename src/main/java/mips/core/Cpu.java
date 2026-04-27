package mips.core;

public class Cpu {
    private final RegisterFile registers = new RegisterFile();
    private final Memory memory = new Memory();
    private int pc = 0; // Program Counter (счетчик команд)

    public void reset() {
        registers.reset();
        memory.reset();
        pc = 0;
    }

    public RegisterFile getRegisters() { return registers; }
    public Memory getMemory() { return memory; }
    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
}
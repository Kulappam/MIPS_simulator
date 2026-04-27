package mips.core;

@FunctionalInterface
public interface Instruction {
    void execute(Cpu cpu, String[] args);
}
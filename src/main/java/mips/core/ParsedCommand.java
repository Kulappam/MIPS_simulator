package mips.core;

public record ParsedCommand(InstructionType type, int[] regIndices, int immediate) {
}
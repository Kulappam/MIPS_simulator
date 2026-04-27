package mips.core;

public class RegisterFile {
    private final int[] registers = new int[32];

    public int read(int index) {
        if (index < 0 || index >= 32) return 0;
        return registers[index];
    }

    public void write(int index, int value) {
        // Регистра $0 (index 0) всегда остается нулем
        if (index > 0 && index < 32) {
            registers[index] = value;
        }
    }

    public void reset() {
        for (int i = 0; i < 32; i++) {
            registers[i] = 0;
        }
    }

    public int[] getAll() {
        return registers;
    }
}
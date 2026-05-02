package mips.core;

public interface MemoryListener {
    void onMemoryChanged(int address, int value);

    void onMemoryReset();
}
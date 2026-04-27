package mips.core;

import java.util.HashMap;
import java.util.Map;

public class Memory {
    // Используем Map, чтобы не выделять сразу гигабайты массива
    private final Map<Integer, Integer> data = new HashMap<>();

    public void writeWord(int address, int value) {
        data.put(address, value);
    }

    public int readWord(int address) {
        return data.getOrDefault(address, 0);
    }

    public void reset() {
        data.clear();
    }
}
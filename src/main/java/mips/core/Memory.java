package mips.core;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import mips.exceptions.*;

/**
 * Единое адресное пространство памяти MIPS.
 * Адреса 0x00400000 - 0x10000000: область кода (.text)
 * Адреса 0x10010000 - 0x7FFFFFFF: область данных (.data)
 */
public class Memory {
    private final Map<Integer, Byte> bytes = new HashMap<>();
    private final Map<Integer, ParsedCommand> instructions = new HashMap<>();
    private final List<MemoryListener> listeners = new ArrayList<>();

    public static final int TEXT_START = 0x00400000;
    public static final int TEXT_END   = 0x10000000;
    public static final int DATA_START = 0x10010000;
    public static final int DATA_END   = 0x7FFFFFFF;

    // ========== Управление слушателями ==========

    public void addListener(MemoryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MemoryListener listener) {
        listeners.remove(listener);
    }

    // ========== Работа с инструкциями ==========

    public void loadProgram(List<ParsedCommand> program, int startAddress) {
        instructions.clear();
        int address = startAddress;
        for (ParsedCommand cmd : program) {
            instructions.put(address, cmd);
            address += 4;
        }
    }

    public ParsedCommand fetchInstruction(int pc) {
        if (pc < TEXT_START || pc >= DATA_START) {
            throw new AddressException("MEMORY: PC Out Of Address Range: 0x" + Integer.toHexString(pc));
        }
        ParsedCommand cmd = instructions.get(pc);
        if (cmd == null) {
            throw new AddressException("MEMORY: Instruction on PC Address Does Not Exist: 0x" + Integer.toHexString(pc));
        }
        return cmd;
    }

    // ========== Работа с данными ==========

    public int readWord(int address) {
        if (address < DATA_START || address >= DATA_END) {
            throw new AddressException("MEMORY: Attempt to Read Data Outside of .data Area: 0x" + Integer.toHexString(address));
        }
        if (address % 4 != 0) {
            throw new AddressException("MEMORY: Unaligned Memory Read Access: 0x" + Integer.toHexString(address));
        }

        int b0 = Byte.toUnsignedInt(bytes.getOrDefault(address,     (byte) 0));
        int b1 = Byte.toUnsignedInt(bytes.getOrDefault(address + 1, (byte) 0));
        int b2 = Byte.toUnsignedInt(bytes.getOrDefault(address + 2, (byte) 0));
        int b3 = Byte.toUnsignedInt(bytes.getOrDefault(address + 3, (byte) 0));

        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public void writeWord(int address, int value) {
        if (address < DATA_START || address >= DATA_END) {
            throw new AddressException("MEMORY: Attempt to Write Data outside of .data Area: 0x" + Integer.toHexString(address));
        }
        if (address % 4 != 0) {
            throw new AddressException("MEMORY: Unaligned Memory Write Access: 0x" + Integer.toHexString(address));
        }

        bytes.put(address,     (byte) ((value >> 24) & 0xFF));
        bytes.put(address + 1, (byte) ((value >> 16) & 0xFF));
        bytes.put(address + 2, (byte) ((value >> 8)  & 0xFF));
        bytes.put(address + 3, (byte) (value & 0xFF));

        notifyMemoryChanged(address, value);
    }

    // ========== Сброс ==========

    public void reset() {
        bytes.clear();
        instructions.clear();
        notifyMemoryReset();
    }

    // ========== Для GUI ==========

    public int getInstructionCount() {
        return instructions.size();
    }

    public Map<Integer, Byte> getBytes() {
        return new HashMap<>(bytes);
    }

    // ========== Уведомления ==========

    private void notifyMemoryChanged(int address, int value) {
        for (MemoryListener l : listeners) {
            l.onMemoryChanged(address, value);
        }
    }

    private void notifyMemoryReset() {
        for (MemoryListener l : listeners) {
            l.onMemoryReset();
        }
    }
}
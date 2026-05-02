package mips.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import mips.exceptions.AddressException;

/**
 * Единое адресное пространство памяти MIPS.
 * Адреса 0x00400000 - 0x10000000: область кода (.text)
 * Адреса 0x10010000 - 0x7FFFFFFF: область данных (.data)
 */
public class Memory {
    // Хранилище байтов (как в твоей DataMemory)
    private final Map<Integer, Byte> bytes = new HashMap<>();

    // Хранилище инструкций (адрес → инструкция)
    private final Map<Integer, ParsedCommand> instructions = new HashMap<>();

    private final List<MemoryListener> listeners = new ArrayList<>();

    public void addListener(MemoryListener listener) {
        listeners.add(listener);
    }

    // Границы областей (стандартные адреса MIPS)
    public static final int TEXT_START = 0x00400000;
    public static final int TEXT_END   = 0x10000000;
    public static final int DATA_START = 0x10010000;
    public static final int DATA_END   = 0x7FFFFFFF;

    /**
     * Загрузка программы в память инструкций
     * @param program список разобранных инструкций
     * @param startAddress начальный адрес (обычно TEXT_START)
     */
    public void loadProgram(java.util.List<ParsedCommand> program, int startAddress) {
        instructions.clear();
        int address = startAddress;
        for (ParsedCommand cmd : program) {
            instructions.put(address, cmd);
            address += 4; // MIPS инструкция = 4 байта
        }
    }

    /**
     * Чтение инструкции по адресу (fetch)
     */
    public ParsedCommand fetchInstruction(int pc) {
        if (pc < TEXT_START || pc >= DATA_START) {
            throw new AddressException("Program Counter вне области кода: 0x" + Integer.toHexString(pc));
        }
        ParsedCommand cmd = instructions.get(pc);
        if (cmd == null) {
            throw new AddressException("Нет инструкции по адресу 0x" + Integer.toHexString(pc));
        }
        return cmd;
    }

    /**
     * Чтение слова (4 байта) из памяти данных
     */
    public int readWord(int address) {
        if (address < DATA_START || address >= DATA_END) {
            throw new AddressException("Попытка чтения данных вне области .data: 0x" + Integer.toHexString(address));
        }
        // Выравнивание адреса (должен быть кратен 4)
        if (address % 4 != 0) {
            throw new AddressException("Невыровненный доступ к памяти: 0x" + Integer.toHexString(address));
        }

        int b0 = Byte.toUnsignedInt(bytes.getOrDefault(address,     (byte) 0));
        int b1 = Byte.toUnsignedInt(bytes.getOrDefault(address + 1, (byte) 0));
        int b2 = Byte.toUnsignedInt(bytes.getOrDefault(address + 2, (byte) 0));
        int b3 = Byte.toUnsignedInt(bytes.getOrDefault(address + 3, (byte) 0));

        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    /**
     * Запись слова (4 байта) в память данных
     */
    public void writeWord(int address, int value) {
        if (address < DATA_START || address >= DATA_END) {
            throw new AddressException("Попытка записи данных вне области .data: 0x" + Integer.toHexString(address));
        }
        if (address % 4 != 0) {
            throw new AddressException("Невыровненный доступ к памяти: 0x" + Integer.toHexString(address));
        }

        bytes.put(address,     (byte) ((value >> 24) & 0xFF));
        bytes.put(address + 1, (byte) ((value >> 16) & 0xFF));
        bytes.put(address + 2, (byte) ((value >> 8)  & 0xFF));
        bytes.put(address + 3, (byte) (value & 0xFF));

        for (MemoryListener l : listeners) {
            l.onMemoryChanged(address, value);
        }
    }

    /**
     * Сброс памяти (очистка данных и инструкций)
     */
    public void reset() {
        bytes.clear();
        instructions.clear();

        for (MemoryListener l: listeners) {
            l.onMemoryReset();
        }
    }

    // Для отладки и GUI
    public int getInstructionCount() {
        return instructions.size();
    }

    public Map<Integer, Byte> getBytes() {
        return new HashMap<>(bytes); // копия для чтения
    }
}
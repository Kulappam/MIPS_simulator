package mips.core;

/**
 * Слушатель событий процессора.
 * GUI реализует этот интерфейс и подписывается на Cpu.
 */
public interface CpuListener {
    /**
     * Вызывается после изменения любого регистра
     */
    void onRegistersChanged(int[] allRegisters);

    /**
     * Вызывается после изменения Program Counter
     */
    void onPcChanged(int pc);

    /**
     * Вызывается после выполнения инструкции (содержит детали)
     */
    void onInstructionExecuted(ParsedCommand command);

    // ========== События для визуализации DataPath ==========

    /**
     * Стадия FETCH: извлечение инструкции по адресу PC
     */
    void onFetch(int pc, ParsedCommand command);

    /**
     * Стадия DECODE: декодирование инструкции
     */
    void onDecode(ParsedCommand command);

    /**
     * Стадия EXECUTE: операция ALU
     */
    void onExecute(int aluOperand1, int aluOperand2, int aluResult, String operation);

    /**
     * Стадия MEMORY: доступ к памяти данных (для lw/sw)
     */
    void onMemoryAccess(int address, int value, boolean isRead);

    /**
     * Стадия WRITEBACK: запись результата в регистр
     */
    void onWriteBack(int registerIndex, int value);

    void onHalted();
}
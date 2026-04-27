package mips.core;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private final Map<String, Instruction> instructions = new HashMap<>();

    public Parser() {
        // Команда LI (Load Immediate): li $t0, 5
        instructions.put("li", (cpu, args) -> {
            int reg = parseReg(args[0]);
            int val = Integer.parseInt(args[1]);
            cpu.getRegisters().write(reg, val);
        });

        // Команда ADD: add $t0, $t1, $t2
        instructions.put("add", (cpu, args) -> {
            int dest = parseReg(args[0]);
            int s1 = cpu.getRegisters().read(parseReg(args[1]));
            int s2 = cpu.getRegisters().read(parseReg(args[2]));
            cpu.getRegisters().write(dest, s1 + s2);
        });

        // Команда SUB: sub $t0, $t1, $t2
        instructions.put("sub", (cpu, args) -> {
            int dest = parseReg(args[0]);
            int s1 = cpu.getRegisters().read(parseReg(args[1]));
            int s2 = cpu.getRegisters().read(parseReg(args[2]));
            cpu.getRegisters().write(dest, s1 - s2);
        });
    }

    public void parseAndExecute(Cpu cpu, String line) {
        if (line.trim().isEmpty()) return;

        // Чистим строку от запятых и лишних пробелов
        String cleanLine = line.replace(",", " ").replaceAll("\\s+", " ").trim();
        String[] parts = cleanLine.split(" ");
        String op = parts[0].toLowerCase();

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        if (instructions.containsKey(op)) {
            instructions.get(op).execute(cpu, args);
        }
    }

    // Хелпер для перевода "$t0" или "$8" в индекс 8
    private int parseReg(String reg) {
        reg = reg.replace("$", "").toLowerCase();
        // Упрощенная логика: можно расширить под имена t0, s0 и т.д.
        // Пока просто парсим числовой индекс для теста
        try {
            return Integer.parseInt(reg);
        } catch (NumberFormatException e) {
            // Тут позже добавим маппинг имен (t0 = 8, s0 = 16...)
            return 0;
        }
    }
}
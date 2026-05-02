package mips.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    // главный метод класса: парсим текст программы в лист парсенных комманд
    public List<ParsedCommand> parseProgram(String text) {
        List<ParsedCommand> program = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            ParsedCommand cmd = parseLine(line);
            if (cmd != null) {
                program.add(cmd);
            }
        }
        return program;
    }

    private ParsedCommand parseLine(String line) {
        // Убираем комментарии и лишние пробелы
        String cleanLine = line.split("#")[0].trim();
        if (cleanLine.isEmpty()) return null;


        // Чистим от запятых и разбиваем по пробелам
        String[] parts = cleanLine.replace(",", " ").split("\\s+");
        String mnemonic = parts[0].toUpperCase();

        try {
            InstructionType type = InstructionType.valueOf(mnemonic);

            if (type == InstructionType.SYSCALL) {
                return new ParsedCommand(type, new int[0], 0);
            }

            // Собираем аргументы (регистры или константы)
            List<Integer> regs = new ArrayList<>();
            int immediate = 0;

            for (int i = 1; i < parts.length; i++) {
                String arg = parts[i].toLowerCase();

                if (arg.contains("(") && arg.contains(")")) {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(-?\\d+)\\((.*)\\)");
                    java.util.regex.Matcher m = p.matcher(arg);

                    if (m.find()) {
                        immediate = parseImmediate(m.group(1)); // Смещение (0)
                        regs.add(parseRegister(m.group(2)));   // Регистр ($t1)
                    }
                    continue;
                }

                if (arg.startsWith("$")) {
                    regs.add(parseRegister(arg));
                } else {
                    // Если это не регистр, значит число (константа или смещение)
                    immediate = parseImmediate(arg);
                }
            }

            // Переводим List в массив для ParsedCommand
            int[] regIndices = regs.stream().mapToInt(Integer::intValue).toArray();
            return new ParsedCommand(type, regIndices, immediate);

        } catch (IllegalArgumentException e) {
            // самый простой обработчик ошибок в симуляторе
            // TODO сделать полноценную реализацию обработчика ошибок для приложения
            System.err.println("Unknown instruction or error: " + mnemonic);
            return null;
        }
    }

    private int parseRegister(String reg) {
        int num = RegisterAliases.toNumber(reg);
        if (num != -1) {
            return num;
        }
        // Если не нашли, пробуем распарсить как $8 (числовой формат)
        try {
            return Integer.parseInt(reg.replace("$", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown register: " + reg);
        }
    }

    private int parseImmediate(String val) {
        if (val.startsWith("0x")) {
            return Integer.parseInt(val.substring(2), 16);
        }
        return Integer.parseInt(val);
    }
}
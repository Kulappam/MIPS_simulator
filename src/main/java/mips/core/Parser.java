package mips.core;

import java.util.ArrayList;
import java.util.List;

import mips.exceptions.*;

public class Parser {

    public List<ParsedCommand> parseText(String text) throws ParsingException {
        List<ParsedCommand> program = new ArrayList<>();
        String[] lines = text.split("\n");

        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            try {
                ParsedCommand cmd = parseLine(lines[lineNum]);
                if (cmd != null) {
                    program.add(cmd);
                }
            } catch (ParsingException e) {
                throw new ParsingException("PARSER: Error Occurred on Line " + (lineNum + 1) + ": " + e.getMessage());
            }
        }
        return program;
    }

    private ParsedCommand parseLine(String line) throws ParsingException {
        String cleanLine = line.split("#")[0].trim();
        if (cleanLine.isEmpty()) return null;

        String[] parts = cleanLine.replace(",", " ").split("\\s+");
        String mnemonic = parts[0].toUpperCase();

        try {
            InstructionType type = InstructionType.valueOf(mnemonic);

            if (type == InstructionType.SYSCALL) {
                return new ParsedCommand(type, new int[0], 0);
            }

            List<Integer> regs = new ArrayList<>();
            int immediate = 0;

            for (int i = 1; i < parts.length; i++) {
                String arg = parts[i].toLowerCase();

                if (arg.contains("(") && arg.contains(")")) {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(-?\\d+)\\((.*)\\)");
                    java.util.regex.Matcher m = p.matcher(arg);

                    if (m.find()) {
                        immediate = parseImmediate(m.group(1));
                        regs.add(parseRegister(m.group(2)));
                    }
                    continue;
                }

                if (arg.startsWith("$")) {
                    regs.add(parseRegister(arg));
                } else {
                    immediate = parseImmediate(arg);
                }
            }

            int[] regIndices = regs.stream().mapToInt(Integer::intValue).toArray();
            return new ParsedCommand(type, regIndices, immediate);

        } catch (IllegalArgumentException e) {
            throw new ParsingException("PARSER: Unknown Instruction: " + mnemonic);
        }
    }

    private int parseRegister(String reg) throws ParsingException {
        int num = RegisterAliases.toNumber(reg);
        if (num != -1) return num;
        try {
            return Integer.parseInt(reg.replace("$", ""));
        } catch (NumberFormatException e) {
            throw new ParsingException("PARSER: Unknown Register: " + reg);
        }
    }

    private int parseImmediate(String val) throws ParsingException {
        try {
            if (val.startsWith("0x")) {
                return Integer.parseInt(val.substring(2), 16);
            }
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new ParsingException("PARSER: Invalid Number: " + val);
        }
    }
}
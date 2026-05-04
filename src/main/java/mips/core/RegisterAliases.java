package mips.core;

import java.util.HashMap;
import java.util.Map;

public class RegisterAliases {

    private static final Map<String, Integer> REGISTER_TO_NUMBER = new HashMap<>();
    private static final String[] NUMBER_TO_NAME = new String[32];

    static {
        for (int i = 0; i < 32; i++) {
            REGISTER_TO_NUMBER.put("$" + i, i);
        }

        REGISTER_TO_NUMBER.put("$zero", 0);
        REGISTER_TO_NUMBER.put("$at", 1);
        REGISTER_TO_NUMBER.put("$v0", 2);
        REGISTER_TO_NUMBER.put("$v1", 3);
        REGISTER_TO_NUMBER.put("$a0", 4);
        REGISTER_TO_NUMBER.put("$a1", 5);
        REGISTER_TO_NUMBER.put("$a2", 6);
        REGISTER_TO_NUMBER.put("$a3", 7);

        REGISTER_TO_NUMBER.put("$t0", 8);
        REGISTER_TO_NUMBER.put("$t1", 9);
        REGISTER_TO_NUMBER.put("$t2", 10);
        REGISTER_TO_NUMBER.put("$t3", 11);
        REGISTER_TO_NUMBER.put("$t4", 12);
        REGISTER_TO_NUMBER.put("$t5", 13);
        REGISTER_TO_NUMBER.put("$t6", 14);
        REGISTER_TO_NUMBER.put("$t7", 15);

        REGISTER_TO_NUMBER.put("$s0", 16);
        REGISTER_TO_NUMBER.put("$s1", 17);
        REGISTER_TO_NUMBER.put("$s2", 18);
        REGISTER_TO_NUMBER.put("$s3", 19);
        REGISTER_TO_NUMBER.put("$s4", 20);
        REGISTER_TO_NUMBER.put("$s5", 21);
        REGISTER_TO_NUMBER.put("$s6", 22);
        REGISTER_TO_NUMBER.put("$s7", 23);

        REGISTER_TO_NUMBER.put("$t8", 24);
        REGISTER_TO_NUMBER.put("$t9", 25);
        REGISTER_TO_NUMBER.put("$k0", 26);
        REGISTER_TO_NUMBER.put("$k1", 27);
        REGISTER_TO_NUMBER.put("$gp", 28);
        REGISTER_TO_NUMBER.put("$sp", 29);
        REGISTER_TO_NUMBER.put("$fp", 30);
        REGISTER_TO_NUMBER.put("$ra", 31);

        // ЯВНЫЙ обратный маппинг (номер → имя)
        NUMBER_TO_NAME[0] = "$zero";
        NUMBER_TO_NAME[1] = "$at";
        NUMBER_TO_NAME[2] = "$v0";
        NUMBER_TO_NAME[3] = "$v1";
        NUMBER_TO_NAME[4] = "$a0";
        NUMBER_TO_NAME[5] = "$a1";
        NUMBER_TO_NAME[6] = "$a2";
        NUMBER_TO_NAME[7] = "$a3";
        NUMBER_TO_NAME[8] = "$t0";
        NUMBER_TO_NAME[9] = "$t1";
        NUMBER_TO_NAME[10] = "$t2";
        NUMBER_TO_NAME[11] = "$t3";
        NUMBER_TO_NAME[12] = "$t4";
        NUMBER_TO_NAME[13] = "$t5";
        NUMBER_TO_NAME[14] = "$t6";
        NUMBER_TO_NAME[15] = "$t7";
        NUMBER_TO_NAME[16] = "$s0";
        NUMBER_TO_NAME[17] = "$s1";
        NUMBER_TO_NAME[18] = "$s2";
        NUMBER_TO_NAME[19] = "$s3";
        NUMBER_TO_NAME[20] = "$s4";
        NUMBER_TO_NAME[21] = "$s5";
        NUMBER_TO_NAME[22] = "$s6";
        NUMBER_TO_NAME[23] = "$s7";
        NUMBER_TO_NAME[24] = "$t8";
        NUMBER_TO_NAME[25] = "$t9";
        NUMBER_TO_NAME[26] = "$k0";
        NUMBER_TO_NAME[27] = "$k1";
        NUMBER_TO_NAME[28] = "$gp";
        NUMBER_TO_NAME[29] = "$sp";
        NUMBER_TO_NAME[30] = "$fp";
        NUMBER_TO_NAME[31] = "$ra";
    }

    public static int toNumber(String name) {
        Integer num = REGISTER_TO_NUMBER.get(name);
        return num != null ? num : -1;
    }

    public static String toName(int number) {
        if (number >= 0 && number < 32) {
            return NUMBER_TO_NAME[number];
        }
        return "$" + number;
    }
}
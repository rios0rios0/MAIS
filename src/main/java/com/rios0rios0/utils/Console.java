package com.rios0rios0.utils;

import java.util.List;

public class Console {

    public static String implode(String separator, List<String> data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.size() - 1; i++) {
            if (data.get(i).matches(" *")) {
                sb.append(data.get(i));
                sb.append(separator);
            }
        }
        sb.append(data.get(data.size() - 1).trim());
        return sb.toString();
    }

    public static void showMsgSuccess(String output) {
        System.out.println(Colorize.setColor("[OK]", Colorize.Params.GREEN, true).concat(" ").concat(output));
    }

    public static void showMsgError(String output) {
        System.out.println(Colorize.setColor("[FAIL]", Colorize.Params.RED, true).concat(" ").concat(output));
    }

    public static void showMsgInfo(String output) {
        System.out.println(Colorize.setColor("[INFO]", Colorize.Params.BLUE, true).concat(" ").concat(output));
    }

    public static void showSyn(String output) {
        System.out.println(Colorize.setColor(" SYN ", Colorize.Params.BROWN, false, false, true).concat(" ").concat(output));
    }

    public static void showAck(String output) {
        System.out.println(Colorize.setColor(" ACK ", Colorize.Params.GRAY, false, false, true).concat(" ").concat(output));
    }

    public static void showMsg(String output) {
        System.out.println(output);
    }
}
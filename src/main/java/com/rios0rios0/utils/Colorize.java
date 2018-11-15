package com.rios0rios0.utils;

public class Colorize {

    public enum Params {
        BOLD(1),
        UKNOW(2),
        ITALIC(3),
        UNDERLINE(4),
        BLACK(30),
        RED(31),
        GREEN(32),
        BROWN(33),
        BLUE(34),
        PURPLE(35),
        CYAN(36),
        GRAY(37);

        /*Dark_Gray(1;30)
        Light_Blue(1;34),
        Light_Green(1;32),
        Light_Cyan(1;36),
        Light_Red(1;31),
        Light_Purple(1;35),
        Yellow(1;33),
        White(1;37);*/

        private final int value;

        Params(int value) {
            this.value = value;
        }

        private int getValue() {
            return value;
        }
    }

    static String setColor(String output, Params color, boolean bold, boolean italic, boolean underline) {
        String response = String.format("\033[%d", color.getValue());
        if (bold) response = response.concat(String.format(";%d", Params.BOLD.getValue()));
        if (italic) response = response.concat(String.format(";%d", Params.ITALIC.getValue()));
        if (underline) response = response.concat(String.format(";%d", Params.UNDERLINE.getValue()));
        return response.concat(String.format("m%s\033[0m", output));
    }

    static String setColor(String output, Params color, boolean bold, boolean italic) {
        return setColor(output, color, bold, italic, false);
    }

    static String setColor(String output, Params color, boolean bold) {
        return setColor(output, color, bold, false);
    }

    static String setColor(String output, Params color) {
        return setColor(output, color, false);
    }
}
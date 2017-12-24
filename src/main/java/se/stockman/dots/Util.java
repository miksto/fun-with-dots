package se.stockman.dots;

public class Util {

    public static int clamp(int value, int minValue, int maxValue) {
        return (value > maxValue) ? maxValue :
            ((value < minValue) ? minValue : value);
    }

    public static int mapToColor(int value, int max) {
        return (int) ((value / (float) max) * 255);
    }
}

package gptui.core.util;

public class LogUtils {
    private static final int MAX_LENGTH = 30;

    private LogUtils() {
    }

    public static String shorten(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= MAX_LENGTH) {
            return s;
        }
        var marker = String.format("...(%d)", s.length());
        var newLength = MAX_LENGTH - marker.length();
        return s.substring(0, newLength) + marker;
    }
}

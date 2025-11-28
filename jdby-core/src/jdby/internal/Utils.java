package jdby.internal;

public final class Utils {

    private static boolean isSpace(char ch) {
        return ch <= ' ';
    }

    public static void append(StringBuilder buf, CharSequence sql) {
        if (!buf.isEmpty() && !sql.isEmpty()) {
            char lastChar = buf.charAt(buf.length() - 1);
            char firstChar = sql.charAt(0);
            if (!isSpace(lastChar) && !isSpace(firstChar)) {
                buf.append(' ');
            }
        }
        buf.append(sql);
    }
}

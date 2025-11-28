package jdby.internal;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String methodString(Method method) {
        return
            "'" + method.getDeclaringClass().getName() + "." + method.getName() +
            "(" +
            Stream.of(method.getParameters()).map(p -> p.getType().getTypeName()).collect(Collectors.joining(", ")) +
            ")'";
    }
}

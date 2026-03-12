package jdby.testing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class SqlDetailsException extends Exception {

    public final Method method;
    public final String sql;

    public SqlDetailsException(Exception cause, Method method, String sql) {
        super(cause);
        this.method = method;
        this.sql = sql;
    }

    private int getCauseFrame(StackTraceElement[] stackTrace) {
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            if (Objects.equals(ste.getClassName(), method.getDeclaringClass().getName())) {
                return i;
            }
        }
        return -1;
    }

    public Exception prettyExceptionForTest(boolean preserveCause, boolean preserveStackTrace) {
        Throwable cause = getCause();
        Throwable newCause = preserveCause ? cause : null;
        StackTraceElement[] stackTrace;
        if (preserveStackTrace) {
            StackTraceElement[] origStackTrace = cause.getStackTrace();
            int causeFrame = getCauseFrame(origStackTrace);
            if (causeFrame >= 0) {
                stackTrace = Arrays.copyOf(origStackTrace, causeFrame + 1);
            } else {
                stackTrace = origStackTrace;
            }
        } else {
            stackTrace = new StackTraceElement[0];
        }
        return new SqlPrettyException(getPrettyText(), newCause, stackTrace);
    }

    public Exception prettyExceptionForTest() {
        return prettyExceptionForTest(false, false);
    }

    public String getLocation() {
        StringBuilder buf = new StringBuilder(method.getDeclaringClass().getName() + "." + method.getName());

        Throwable cause = getCause();
        StackTraceElement[] stackTrace = cause.getStackTrace();
        int causeFrame = getCauseFrame(stackTrace);
        if (causeFrame >= 0) {
            buf.append(" (line " + stackTrace[causeFrame].getLineNumber() + ")");
        }

        return buf.toString();
    }

    public String getErrorMessage() {
        Throwable cause = getCause();
        return cause instanceof SQLException ? cause.getMessage() : cause.toString();
    }

    public void prettyPrint(PrintWriter pw) {
        pw.println("Error at " + getLocation() + ": " + getErrorMessage());
        if (sql != null) {
            pw.println(String.join("", Collections.nCopies(10, "-")));
            pw.println(sql.stripIndent().trim());
        }
        pw.println(String.join("", Collections.nCopies(10, "=")));
    }

    public String getPrettyText() {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            prettyPrint(pw);
        }
        return sw.toString().trim();
    }

    @Override
    public String toString() {
        return getPrettyText();
    }
}

package jdby.testing;

public class SqlPrettyException extends Exception {

    public SqlPrettyException(String message, Throwable cause, StackTraceElement[] stackTrace) {
        super(message, cause, true, true);
        setStackTrace(stackTrace);
    }

    public SqlPrettyException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}

package jdby.core;

public class UncheckedSQLException extends RuntimeException {

    public UncheckedSQLException(String message) {
        super(message);
    }

    public UncheckedSQLException(Throwable cause) {
        super(cause);
    }
}

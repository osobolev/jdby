package jdby.core;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

    public UncheckedSQLException(String message) {
        super(message);
    }

    public UncheckedSQLException(SQLException cause) {
        super(cause);
    }

    public SQLException getCause() {
        return (SQLException) super.getCause();
    }

    public String getSQLState() {
        SQLException cause = getCause();
        return cause == null ? null : cause.getSQLState();
    }
}

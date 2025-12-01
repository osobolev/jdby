package jdby.core;

import java.sql.SQLException;

public class UncheckedSQLException extends RuntimeException {

    private final String sqlState;

    public UncheckedSQLException(String sqlState, String message) {
        super(message);
        this.sqlState = sqlState;
    }

    public UncheckedSQLException(SQLException cause) {
        super(cause);
        this.sqlState = cause.getSQLState();
    }

    public SQLException getCause() {
        return (SQLException) super.getCause();
    }

    public String getSQLState() {
        return sqlState;
    }
}

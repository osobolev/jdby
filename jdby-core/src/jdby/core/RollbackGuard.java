package jdby.core;

import java.sql.Connection;
import java.sql.SQLException;

public final class RollbackGuard implements AutoCloseable {

    private final Connection connection;

    private boolean ok = false;

    public RollbackGuard(Connection connection) {
        this.connection = connection;
    }

    public void ok() {
        this.ok = true;
    }

    @Override
    public void close() throws SQLException {
        if (!ok) {
            connection.rollback();
        }
    }
}

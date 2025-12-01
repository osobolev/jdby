package jdby.internal;

import jdby.core.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class RollbackGuard implements AutoCloseable {

    private final Connection connection;
    private final ConnectionFactory connectionFactory;

    private boolean ok = false;

    private RollbackGuard(Connection connection, ConnectionFactory connectionFactory) {
        this.connection = connection;
        this.connectionFactory = connectionFactory;
    }

    public static RollbackGuard create(ConnectionFactory connectionFactory) throws SQLException {
        Connection connection = connectionFactory.openConnection();
        return new RollbackGuard(connection, connectionFactory);
    }

    public Connection getConnection() {
        return connection;
    }

    public void ok() {
        this.ok = true;
    }

    private void releaseConnection() throws SQLException {
        connectionFactory.closeConnection(connection);
    }

    @Override
    public void close() throws SQLException {
        try {
            if (ok) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            try {
                releaseConnection();
            } catch (SQLException ex2) {
                ex.addSuppressed(ex2);
            }
            throw ex;
        }
        releaseConnection();
    }
}

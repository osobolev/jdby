package jdbq.core;

import java.sql.Connection;

public class SimpleSqlTransaction implements SqlTransaction {

    private final Connection connection;

    public SimpleSqlTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}

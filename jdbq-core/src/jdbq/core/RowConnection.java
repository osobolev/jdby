package jdbq.core;

import java.sql.Connection;

public class RowConnection {

    private final RowContext ctx;
    private final Connection connection;

    public RowConnection(RowContext ctx, Connection connection) {
        this.ctx = ctx;
        this.connection = connection;
    }

    public <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return ctx.rowMapper(rowType);
    }

    public <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType) {
        return ctx.keyMapper(keyType);
    }

    public Connection getConnection() {
        return connection;
    }
}

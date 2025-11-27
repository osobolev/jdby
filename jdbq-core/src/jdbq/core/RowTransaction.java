package jdbq.core;

import java.sql.Connection;
import java.sql.SQLException;

public class RowTransaction implements SqlTransaction {

    private final RowContext ctx;
    private final SqlTransaction t;

    public RowTransaction(RowContext ctx, SqlTransaction t) {
        this.ctx = ctx;
        this.t = t;
    }

    public <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return ctx.rowMapper(rowType);
    }

    public <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType) {
        return ctx.keyMapper(keyType);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return t.getConnection();
    }
}

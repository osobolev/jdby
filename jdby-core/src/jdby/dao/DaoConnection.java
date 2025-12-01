package jdby.dao;

import jdby.core.GeneratedKeyMapper;
import jdby.core.RowConnection;
import jdby.core.RowMapper;

import java.sql.Connection;

public class DaoConnection implements RowConnection {

    private final DaoContext ctx;
    private final Connection connection;

    public DaoConnection(DaoContext ctx, Connection connection) {
        this.ctx = ctx;
        this.connection = connection;
    }

    @Override
    public <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return ctx.rowMapper(rowType);
    }

    @Override
    public <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType) {
        return ctx.keyMapper(keyType);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public <T> T dao(Class<T> cls) {
        // todo: cache proxies???
        return DaoProxies.createProxy(ctx, cls, getConnection());
    }
}

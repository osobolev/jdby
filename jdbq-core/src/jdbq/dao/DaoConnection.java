package jdbq.dao;

import jdbq.core.RowConnection;

import java.sql.Connection;

public class DaoConnection extends RowConnection {

    private final DaoContext ctx;

    public DaoConnection(DaoContext ctx, Connection connection) {
        super(ctx, connection);
        this.ctx = ctx;
    }

    public <T> T dao(Class<T> cls) {
        // todo: cache proxies???
        return DaoSql.createProxy(ctx, cls, getConnection());
    }
}

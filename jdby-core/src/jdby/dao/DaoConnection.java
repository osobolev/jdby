package jdby.dao;

import java.sql.Connection;

public class DaoConnection {

    private final DaoContext ctx;
    private final Connection connection;

    public DaoConnection(DaoContext ctx, Connection connection) {
        this.ctx = ctx;
        this.connection = connection;
    }

    public DaoContext getContext() {
        return ctx;
    }

    public Connection getConnection() {
        return connection;
    }

    public <T> T dao(Class<T> cls) {
        // todo: cache proxies???
        return DaoProxies.createProxy(ctx, connection, cls);
    }
}

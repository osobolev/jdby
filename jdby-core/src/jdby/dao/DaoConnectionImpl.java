package jdby.dao;

import java.sql.Connection;

public class DaoConnectionImpl implements DaoConnection {

    private final DaoContext ctx;
    private final Connection connection;

    public DaoConnectionImpl(DaoContext ctx, Connection connection) {
        this.ctx = ctx;
        this.connection = connection;
    }

    public DaoContext getContext() {
        return ctx;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public <T> T dao(Class<T> daoInterface) {
        // todo: cache proxies???
        return DaoProxies.createProxy(ctx, connection, daoInterface);
    }
}

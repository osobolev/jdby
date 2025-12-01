package jdby.mapping;

import jdby.core.GeneratedKeyMapper;
import jdby.core.RowConnection;
import jdby.core.RowMapper;

import java.sql.Connection;

public class MapperConnection implements RowConnection {

    private final MapperContext ctx;
    private final Connection connection;

    public MapperConnection(MapperContext ctx, Connection connection) {
        this.ctx = ctx;
        this.connection = connection;
    }

    public MapperContext getContext() {
        return ctx;
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
}

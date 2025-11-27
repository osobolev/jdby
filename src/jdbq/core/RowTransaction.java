package jdbq.core;

import java.sql.Connection;
import java.sql.SQLException;

public class RowTransaction implements SqlTransaction {

    private final RowMapperFactory rowMapperFactory;
    private final SqlTransaction t;

    public RowTransaction(RowMapperFactory rowMapperFactory, SqlTransaction t) {
        this.rowMapperFactory = rowMapperFactory;
        this.t = t;
    }

    public <T> RowMapper<T> mapper(Class<T> rowType) {
        return rowMapperFactory.mapper(rowType);
    }

    public <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType) {
        return rowMapperFactory.keyMapper(keyType);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return t.getConnection();
    }
}

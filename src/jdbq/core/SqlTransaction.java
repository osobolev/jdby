package jdbq.core;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlTransaction implements SqlTransactionRaw {

    private final RowMapperFactory rowMapperFactory;
    private final SqlTransactionRaw t;

    public SqlTransaction(RowMapperFactory rowMapperFactory, SqlTransactionRaw t) {
        this.rowMapperFactory = rowMapperFactory;
        this.t = t;
    }

    public <T> RowMapper<T> mapper(Class<T> rowType) {
        return rowMapperFactory.mapper(rowType);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return t.getConnection();
    }
}

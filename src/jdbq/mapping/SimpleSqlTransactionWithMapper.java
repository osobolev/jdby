package jdbq.mapping;

import jdbq.core.RowMapper;
import jdbq.core.RowMapperFactory;
import jdbq.core.SqlTransactionRaw;
import jdbq.core.SqlTransaction;

import java.sql.Connection;
import java.sql.SQLException;

// todo: move it to SqlTransaction as a class???
public class SimpleSqlTransactionWithMapper implements SqlTransaction {

    private final RowMapperFactory rowMapperFactory;
    private final SqlTransactionRaw t;

    public SimpleSqlTransactionWithMapper(RowMapperFactory rowMapperFactory, SqlTransactionRaw t) {
        this.rowMapperFactory = rowMapperFactory;
        this.t = t;
    }

    public SimpleSqlTransactionWithMapper(SqlTransactionRaw t) {
        this(new RecordMapperFactory(), t);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return t.getConnection();
    }

    @Override
    public <T> RowMapper<T> mapper(Class<T> rowType) {
        return rowMapperFactory.mapper(rowType);
    }
}

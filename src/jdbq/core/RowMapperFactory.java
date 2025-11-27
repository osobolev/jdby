package jdbq.core;

public interface RowMapperFactory {

    <T> RowMapper<T> mapper(Class<T> rowType);

    default SqlTransaction withTransaction(SqlTransactionRaw t) {
        return new SqlTransaction(this, t);
    }
}

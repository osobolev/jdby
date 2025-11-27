package jdbq.core;

public interface RowMapperFactory {

    <T> RowMapper<T> mapper(Class<T> rowType);

    default RowTransaction withTransaction(SqlTransaction t) {
        return new RowTransaction(this, t);
    }
}

package jdbq.core;

public interface RowMapperFactory {

    <T> RowMapper<T> mapper(Class<T> rowType);

    <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType);

    default RowTransaction withTransaction(SqlTransaction t) {
        return new RowTransaction(this, t);
    }
}

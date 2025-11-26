package jdbq.core;

public interface RowMapperFactory {

    <T> RowMapper<T> mapper(Class<T> rowType);
}

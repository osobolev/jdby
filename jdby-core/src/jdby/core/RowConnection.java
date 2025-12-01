package jdby.core;

import java.sql.Connection;

public interface RowConnection {

    <T> RowMapper<T> rowMapper(Class<T> rowType);

    <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType);

    Connection getConnection();
}

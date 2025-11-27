package jdbq.core;

import java.sql.Connection;

public interface RowContext {

    <T> RowMapper<T> rowMapper(Class<T> rowType);

    <T> GeneratedKeyMapper<T> keyMapper(Class<T> keyType);

    default RowConnection withConnection(Connection connection) {
        return new RowConnection(this, connection);
    }
}

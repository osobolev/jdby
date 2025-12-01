package jdby.mapping;

import jdby.core.GeneratedKeyMapper;
import jdby.core.RowConnection;
import jdby.core.RowMapper;
import jdby.core.testing.SqlTestingHook;
import jdby.transaction.ConnectionFactory;
import jdby.transaction.SqlTransaction;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface MapperContext {

    <T> RowMapper<T> rowMapper(Class<T> rowType);

    ColumnMapper columnMapper(Type type);

    default <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls) {
        ColumnMapper columnMapper = columnMapper(cls);
        return (rows, columns, rs) -> {
            if (SqlTestingHook.isTesting()) {
                SqlTestingHook.hook.checkColumn(rs, cls, columnMapper);
                return SqlTestingHook.mock(cls);
            }
            if (rs.next()) {
                return cls.cast(columnMapper.getColumn(rs, 1));
            } else {
                return null;
            }
        };
    }

    default RowConnection withConnection(Connection connection) {
        return new RowConnectionImpl(this, connection);
    }

    default <E extends Exception> SqlTransaction<RowConnection> transaction(ConnectionFactory dataSource) throws E {
        return new SqlTransaction<>(dataSource, this::withConnection);
    }

    static DefaultMapperContextBuilder builder() {
        return new DefaultMapperContextBuilder();
    }
}

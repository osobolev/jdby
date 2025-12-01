package jdby.mapping;

import jdby.core.GeneratedKeyMapper;
import jdby.core.RowConnection;
import jdby.core.RowMapper;
import jdby.core.testing.SqlTestingHook;
import jdby.transaction.ConnectionFactory;

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
        return new MapperConnection(this, connection);
    }

    interface RowAction<E extends Exception> extends ConnectionFactory.SqlAction<RowConnection, E> {
    }

    interface RowFunction<R, E extends Exception> extends ConnectionFactory.SqlFunction<RowConnection, R, E> {
    }

    default <E extends Exception> void transaction(ConnectionFactory dataSource, RowAction<E> action) throws E {
        ConnectionFactory.transactionAction(dataSource, this::withConnection, action);
    }

    default <R, E extends Exception> R transactionCall(ConnectionFactory dataSource, RowFunction<R, E> call) throws E {
        return ConnectionFactory.transactionCall(dataSource, this::withConnection, call);
    }

    static DefaultMapperContextBuilder builder() {
        return new DefaultMapperContextBuilder();
    }
}

package jdby.dao;

import jdby.core.Batch;
import jdby.core.ConnectionFactory;
import jdby.core.GeneratedKeyMapper;
import jdby.core.RowMapper;
import jdby.core.SqlParameter;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface DaoContext {

    <T> RowMapper<T> rowMapper(Class<T> rowType);

    <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls);

    default boolean nonSqlParameter(Type type, Object value) {
        return type == Batch.class;
    }

    ParameterMapper parameterMapper(Type type);

    default SqlParameter parameter(Type type, Object value) {
        return parameterMapper(type).toSql(value);
    }

    default DaoConnection withConnection(Connection connection) {
        return new DaoConnection(this, connection);
    }

    interface DaoAction<E extends Exception> extends ConnectionFactory.SqlAction<DaoConnection, E> {
    }

    interface DaoFunction<R, E extends Exception> extends ConnectionFactory.SqlFunction<DaoConnection, R, E> {
    }

    default <E extends Exception> void transaction(ConnectionFactory dataSource, DaoAction<E> action) throws E {
        ConnectionFactory.transactionAction(dataSource, this::withConnection, action);
    }

    default <R, E extends Exception> R transactionCall(ConnectionFactory dataSource, DaoFunction<R, E> call) throws E {
        return ConnectionFactory.transactionCall(dataSource, this::withConnection, call);
    }
}

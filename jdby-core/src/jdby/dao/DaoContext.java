package jdby.dao;

import jdby.core.*;
import jdby.mapping.MapperContext;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface DaoContext {

    MapperContext getMapperContext();

    default <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return getMapperContext().rowMapper(rowType);
    }

    default <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls) {
        return getMapperContext().keyMapper(cls);
    }

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

    static DefaultDaoContextBuilder builder() {
        return new DefaultDaoContextBuilder();
    }
}

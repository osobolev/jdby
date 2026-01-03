package jdby.dao;

import jdby.core.Batch;
import jdby.core.SqlParameter;
import jdby.mapping.MapperContext;
import jdby.transaction.ConnectionFactory;
import jdby.transaction.SqlTransaction;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface DaoContext {

    MapperContext getMapperContext();

    default boolean nonSqlParameter(Type type, Object value) {
        return type == Batch.class;
    }

    ParameterMapper parameterMapper(Type type);

    default SqlParameter parameter(Type type, Object value) {
        if (type == SqlParameter.class) {
            return (SqlParameter) value;
        }
        return parameterMapper(type).toSql(value);
    }

    default DaoConnection withConnection(Connection connection) {
        return new DaoConnectionImpl(this, connection);
    }

    /**
     * Allocates new connection from the {@code dataSource} and commits the transaction
     * for each DAO method call.
     */
    default DaoSource withDataSource(ConnectionFactory dataSource) {
        return new DaoSource() {
            @Override
            public <T> T dao(Class<T> iface) {
                return DaoProxies.createProxy(DaoContext.this, dataSource, true, iface);
            }
        };
    }

    default <E extends Exception> SqlTransaction<DaoConnection> transaction(ConnectionFactory dataSource) throws E {
        return new SqlTransaction<>(dataSource, this::withConnection);
    }

    static DefaultDaoContextBuilder builder() {
        return new DefaultDaoContextBuilder();
    }
}

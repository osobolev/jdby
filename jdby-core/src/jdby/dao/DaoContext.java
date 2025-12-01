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

    default <E extends Exception> SqlTransaction<DaoConnection> transaction(ConnectionFactory dataSource) throws E {
        return new SqlTransaction<>(dataSource, this::withConnection);
    }

    static DefaultDaoContextBuilder builder() {
        return new DefaultDaoContextBuilder();
    }
}

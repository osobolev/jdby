package jdby.dao;

import jdby.core.Batch;
import jdby.core.SqlParameter;
import jdby.mapping.MapperContext;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface DaoContext extends MapperContext {

    default boolean nonSqlParameter(Type type, Object value) {
        return type == Batch.class;
    }

    ParameterMapper parameterMapper(Type type);

    default SqlParameter parameter(Type type, Object value) {
        return parameterMapper(type).toSql(value);
    }

    @Override
    default DaoConnection withConnection(Connection connection) {
        return new DaoConnection(this, connection);
    }
}

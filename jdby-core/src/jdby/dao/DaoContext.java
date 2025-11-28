package jdby.dao;

import jdby.core.SqlParameter;
import jdby.mapping.MapperContext;

import java.lang.reflect.Type;
import java.sql.Connection;

public interface DaoContext extends MapperContext {

    ParameterMapper parameterMapper(Type type);

    default SqlParameter parameter(Type type, Object value) {
        return parameterMapper(type).toSql(value);
    }

    @Override
    default DaoConnection withConnection(Connection connection) {
        return new DaoConnection(this, connection);
    }
}

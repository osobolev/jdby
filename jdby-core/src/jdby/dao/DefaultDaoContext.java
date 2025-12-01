package jdby.dao;

import jdby.core.SqlParameter;
import jdby.mapping.MapperContext;

import java.lang.reflect.Type;
import java.util.Map;

public class DefaultDaoContext implements DaoContext {

    private final MapperContext mapperContext;
    private final Map<Type, ParameterMapper> parameterMappers;

    public DefaultDaoContext(MapperContext mapperContext,
                             Map<Type, ParameterMapper> parameterMappers) {
        this.mapperContext = mapperContext;
        this.parameterMappers = parameterMappers;
    }

    @Override
    public MapperContext getMapperContext() {
        return mapperContext;
    }

    @Override
    public ParameterMapper parameterMapper(Type type) {
        if (type == SqlParameter.class) {
            return value -> (SqlParameter) value;
        }
        ParameterMapper parameterMapper = parameterMappers.get(type);
        if (parameterMapper == null) {
            throw new IllegalArgumentException("Cannot create parameter of type '" + type.getTypeName() + "'");
        }
        return parameterMapper;
    }
}

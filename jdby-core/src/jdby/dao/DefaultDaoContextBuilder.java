package jdby.dao;

import jdby.core.SqlParameter;
import jdby.mapping.DefaultMapperContextBuilder;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class DefaultDaoContextBuilder extends DefaultMapperContextBuilder {

    protected final Map<Type, ParameterMapper> parameterMappers = new HashMap<>();

    public DefaultDaoContextBuilder() {
        registerSimple(byte.class, JDBCType.TINYINT);
        registerSimple(Byte.class, JDBCType.TINYINT);
        registerSimple(short.class, JDBCType.SMALLINT);
        registerSimple(Short.class, JDBCType.SMALLINT);
        registerSimple(int.class, JDBCType.INTEGER);
        registerSimple(Integer.class, JDBCType.INTEGER);
        registerSimple(long.class, JDBCType.BIGINT);
        registerSimple(Long.class, JDBCType.BIGINT);
        registerSimple(double.class, JDBCType.DOUBLE);
        registerSimple(Double.class, JDBCType.DOUBLE);
        registerSimple(float.class, JDBCType.FLOAT);
        registerSimple(Float.class, JDBCType.FLOAT);
        registerSimple(boolean.class, JDBCType.BOOLEAN);
        registerSimple(Boolean.class, JDBCType.BOOLEAN);
        registerSimple(BigInteger.class, JDBCType.BIGINT);
        registerSimple(BigDecimal.class, JDBCType.DECIMAL);
        registerSimple(String.class, JDBCType.VARCHAR);
        registerSimple(LocalDate.class, JDBCType.DATE);
        registerSimple(LocalTime.class, JDBCType.TIME);
        registerSimple(LocalDateTime.class, JDBCType.TIMESTAMP);
        registerSimple(OffsetDateTime.class, JDBCType.TIMESTAMP_WITH_TIMEZONE);
        registerSimple(byte[].class, JDBCType.VARBINARY);
    }

    private void registerSimple(Class<?> cls, JDBCType jdbcType) {
        registerParameter(cls, value -> SqlParameter.jdbc(value, jdbcType));
    }

    public DefaultDaoContextBuilder registerParameter(Type type, ParameterMapper parameterMapper) {
        parameterMappers.put(type, parameterMapper);
        return this;
    }

    public DaoContext build() {
        return new DefaultDaoContext(super.buildCore(), Map.copyOf(parameterMappers));
    }
}

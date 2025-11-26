package jdbq.dao;

import jdbq.core.SimpleSqlParameter;
import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;
import jdbq.mapping.RecordMapperFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class DefaultDaoContext extends RecordMapperFactory implements DaoContext {

    public static JDBCType jdbcType(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return JDBCType.INTEGER;
        } else if (type == long.class || type == Long.class || type == BigInteger.class) {
            return JDBCType.BIGINT;
        } else if (type == double.class || type == Double.class) {
            return JDBCType.DOUBLE;
        } else if (type == short.class || type == Short.class) {
            return JDBCType.SMALLINT;
        } else if (type == byte.class || type == Byte.class) {
            return JDBCType.TINYINT;
        } else if (type == float.class || type == Float.class) {
            return JDBCType.FLOAT;
        } else if (type == BigDecimal.class) {
            return JDBCType.DECIMAL;
        } else if (type == String.class) {
            return JDBCType.VARCHAR;
        } else if (type == LocalDate.class) {
            return JDBCType.DATE;
        } else if (type == OffsetDateTime.class) {
            return JDBCType.TIMESTAMP_WITH_TIMEZONE;
        } else if (type == LocalDateTime.class) {
            return JDBCType.TIMESTAMP;
        } else if (type == LocalTime.class) {
            return JDBCType.TIME;
        } else if (type == byte[].class) {
            return JDBCType.VARBINARY;
        } else if (type == boolean.class || type == Boolean.class) {
            return JDBCType.BOOLEAN;
        } else {
            return null;
        }
    }

    @Override
    public SqlParameter parameter(Type type, Object value) {
        if (type instanceof Class<?> cls) {
            JDBCType jdbcType = jdbcType(cls);
            if (jdbcType == null) {
                throw new IllegalArgumentException("Cannot create parameter of class " + cls.getName());
            }
            return new SimpleSqlParameter(value, jdbcType);
        } else {
            throw new IllegalArgumentException("Cannot create parameter of type " + type);
        }
    }

    @Override
    public <T extends BaseDao> T proxy(Class<T> cls, SqlTransactionRaw t) {
        return ProxyQueries.create(this, cls, t);
    }
}

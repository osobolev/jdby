package jdbq.mapping;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

public final class SimpleMappers {

    private static final Set<Class<?>> JDBC_TYPES = Set.of(
        Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class,
        String.class, byte[].class,
        BigDecimal.class, BigInteger.class,
        LocalDate.class, OffsetDateTime.class, LocalDateTime.class
    );

    public static ColumnMapper column(Type type) {
        if (type instanceof Class<?> cls) {
            if (type == byte.class) {
                return ResultSet::getByte;
            } else if (type == short.class) {
                return ResultSet::getShort;
            } else if (type == int.class) {
                return ResultSet::getInt;
            } else if (type == long.class) {
                return ResultSet::getLong;
            } else if (type == float.class) {
                return ResultSet::getFloat;
            } else if (type == double.class) {
                return ResultSet::getDouble;
            } else if (type == boolean.class) {
                return ResultSet::getBoolean;
            } else if (JDBC_TYPES.contains(cls)) {
                return (rs, index) -> rs.getObject(index, cls);
            } else {
                throw new IllegalArgumentException("Unsupported class " + cls.getName() + " for columns");
            }
        } else {
            throw new IllegalArgumentException("Unsupported type " + type + " for columns");
        }
    }
}

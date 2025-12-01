package jdby.mapping;

import jdby.core.RowMapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultMapperContextBuilder {

    protected ColumnNaming columnNaming = ColumnNaming.camelCase();
    protected final Map<Type, ColumnMapper> columnMappers = new HashMap<>();
    protected final Map<Class<?>, RowMapper<?>> rowMappers = new HashMap<>();

    public DefaultMapperContextBuilder() {
        registerSimple(byte.class, ColumnMapper.byteMapper());
        registerSimple(short.class, ColumnMapper.shortMapper());
        registerSimple(int.class, ColumnMapper.intMapper());
        registerSimple(long.class, ColumnMapper.longMapper());
        registerSimple(float.class, ColumnMapper.floatMapper());
        registerSimple(double.class, ColumnMapper.doubleMapper());
        registerSimple(boolean.class, ColumnMapper.booleanMapper());
        List<Class<?>> jdbcTypes = List.of(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class,
            String.class, byte[].class,
            BigDecimal.class, BigInteger.class,
            LocalDate.class, LocalTime.class, OffsetDateTime.class, LocalDateTime.class
        );
        for (Class<?> jdbcType : jdbcTypes) {
            registerColumn(jdbcType, ColumnMapper.jdbcMapper(jdbcType));
        }
    }

    private <T> void registerSimple(Class<T> cls, SimpleColumnMapper<?> columnMapper) {
        registerColumn(cls, columnMapper);
    }

    public DefaultMapperContextBuilder setColumnNaming(ColumnNaming columnNaming) {
        this.columnNaming = columnNaming;
        return this;
    }

    public DefaultMapperContextBuilder registerColumn(Type type, ColumnMapper columnMapper) {
        columnMappers.put(type, columnMapper);
        return this;
    }

    public <T> DefaultMapperContextBuilder registerRow(Class<T> rowType, RowMapper<T> rowMapper) {
        rowMappers.put(rowType, rowMapper);
        return this;
    }

    public MapperContext build() {
        return new DefaultMapperContext(columnNaming, columnMappers, rowMappers);
    }
}

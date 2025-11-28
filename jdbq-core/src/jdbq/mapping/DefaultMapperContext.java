package jdbq.mapping;

import jdbq.core.RowMapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMapperContext implements MapperContext {

    private final ColumnNaming columnNaming;
    private final ConcurrentHashMap<Type, ColumnMapper> columnMappers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();

    public DefaultMapperContext(ColumnNaming columnNaming) {
        this.columnNaming = columnNaming;
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

    public void registerColumn(Type type, ColumnMapper columnMapper) {
        columnMappers.put(type, columnMapper);
    }

    public <T> void registerRow(Class<T> rowType, RowMapper<T> rowMapper) {
        rowMappers.put(rowType, rowMapper);
    }

    @Override
    public ColumnMapper columnMapper(Type type) {
        ColumnMapper columnMapper = columnMappers.get(type);
        if (columnMapper == null) {
            throw new IllegalArgumentException("Unsupported type for columns: " + type);
        }
        return columnMapper;
    }

    protected <T> RowMapper<T> newRowMapper(Class<T> rowType) {
        if (rowType.isRecord()) {
            SqlNameStrategy strategy = rowType.getDeclaredAnnotation(SqlNameStrategy.class);
            ColumnNaming naming;
            if (strategy == null) {
                naming = columnNaming;
            } else {
                Class<? extends ColumnNaming> strategyClass = strategy.value();
                try {
                    naming = strategyClass.getConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            if (!naming.useNames()) {
                return PositionalRecordRowMapper.create(rowType, this::columnMapper);
            } else {
                return NamedRecordRowMapper.create(rowType, columnNaming, this::columnMapper);
            }
        } else {
            ColumnMapper columnMapper = columnMapper(rowType);
            return new SingleColumnMapper<>(rowType, columnMapper);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return (RowMapper<T>) rowMappers.computeIfAbsent(rowType, this::newRowMapper);
    }
}

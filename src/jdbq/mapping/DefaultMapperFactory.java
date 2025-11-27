package jdbq.mapping;

import jdbq.core.RowMapper;
import jdbq.core.SqlTesting;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMapperFactory implements MapperFactory {

    public static volatile CheckColumnCompatibility check = null;

    private final ColumnNaming columnNaming;
    private final ConcurrentHashMap<Type, ColumnMapper> columnMappers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();

    public DefaultMapperFactory(ColumnNaming columnNaming) {
        this.columnNaming = columnNaming;
        registerSimple(SimpleColumnMapper.byteMapper());
        registerSimple(SimpleColumnMapper.shortMapper());
        registerSimple(SimpleColumnMapper.intMapper());
        registerSimple(SimpleColumnMapper.longMapper());
        registerSimple(SimpleColumnMapper.floatMapper());
        registerSimple(SimpleColumnMapper.doubleMapper());
        registerSimple(SimpleColumnMapper.booleanMapper());
        List<Class<?>> jdbcTypes = List.of(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class,
            String.class, byte[].class,
            BigDecimal.class, BigInteger.class,
            LocalDate.class, LocalTime.class, OffsetDateTime.class, LocalDateTime.class
        );
        for (Class<?> jdbcType : jdbcTypes) {
            registerSimple(SimpleColumnMapper.jdbcMapper(jdbcType));
        }
    }

    private <T> void registerSimple(SimpleColumnMapper<T> columnMapper) {
        registerColumn(columnMapper.cls, columnMapper);
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
            throw new IllegalArgumentException("Unsupported type " + type + " for columns");
        }
        return columnMapper;
    }

    @SuppressWarnings("unchecked")
    protected RowMapper<?> newRowMapper(Class<?> rowType) {
        if (rowType.isRecord()) {
            Class<Record> cls = (Class<Record>) rowType;
            SqlNameStrategy strategy = cls.getDeclaredAnnotation(SqlNameStrategy.class);
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
                return PositionalRecordRowMapper.create(cls, this::columnMapper);
            } else {
                return NamedRecordRowMapper.create(cls, columnNaming, this::columnMapper);
            }
        } else {
            ColumnMapper columnMapper = columnMapper(rowType);
            return (RowMapper<Object>) rs -> {
                if (SqlTesting.testing) {
                    CheckCompatibility checker = new CheckCompatibility(rs.getMetaData());
                    checker.checkCompatibility(rowType, "<column>", 1, columnMapper.checkCompatibility());
                    Object array = Array.newInstance(rowType, 1);
                    return Array.get(array, 0);
                }
                return columnMapper.getColumn(rs, 1);
            };
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RowMapper<T> rowMapper(Class<T> rowType) {
        return (RowMapper<T>) rowMappers.computeIfAbsent(rowType, this::newRowMapper);
    }
}

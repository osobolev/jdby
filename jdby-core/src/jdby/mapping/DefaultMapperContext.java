package jdby.mapping;

import jdby.core.RowMapper;

import java.lang.reflect.Type;
import java.util.Map;

public class DefaultMapperContext implements MapperContext {

    private final ColumnNaming columnNaming;
    private final Map<Type, ColumnMapper> columnMappers;
    private final Map<Class<?>, RowMapper<?>> rowMappers;

    public DefaultMapperContext(ColumnNaming columnNaming,
                                Map<Type, ColumnMapper> columnMappers,
                                Map<Class<?>, RowMapper<?>> rowMappers) {
        this.columnNaming = columnNaming;
        this.columnMappers = columnMappers;
        this.rowMappers = rowMappers;
    }

    @Override
    public ColumnMapper columnMapper(Type type) {
        ColumnMapper columnMapper = columnMappers.get(type);
        if (columnMapper == null) {
            throw new IllegalArgumentException("Unsupported type for columns: '" + type.getTypeName() + "'");
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

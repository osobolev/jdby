package jdbq.mapping;

import jdbq.core.RowMapper;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class RecordMapperFactory implements MapperFactory {

    private final ConcurrentHashMap<Class<?>, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();

    @Override
    public ColumnMapper columnMapper(Type type) {
        return ColumnMappers.simple(type);
    }

    @SuppressWarnings("unchecked")
    protected RowMapper<?> newRowMapper(Class<?> rowType) {
        if (rowType.isRecord()) {
            return RecordRowMapper.create((Class<Record>) rowType, this::columnMapper);
        } else {
            ColumnMapper columnMapper = columnMapper(rowType);
            return (RowMapper<Object>) rs -> columnMapper.getColumn(rs, 1);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RowMapper<T> mapper(Class<T> rowType) {
        return (RowMapper<T>) rowMappers.computeIfAbsent(rowType, this::newRowMapper);
    }
}

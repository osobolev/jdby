package jdbq.mapping;

import jdbq.core.RowMapper;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMapperFactory implements MapperFactory {

    private final ConcurrentHashMap<Class<?>, RowMapper<?>> rowMappers = new ConcurrentHashMap<>();

    @Override
    public ColumnMapperPosition positionColumnMapper(Type type) {
        return SimpleMappers.positionColumn(type);
    }

    @Override
    public ColumnMapperName nameColumnMapper(Type type) {
        return SimpleMappers.nameColumn(type);
    }

    @SuppressWarnings("unchecked")
    protected RowMapper<?> newRowMapper(Class<?> rowType) {
        if (rowType.isRecord()) {
            return PositionalRecordRowMapper.create((Class<Record>) rowType, this::positionColumnMapper);
        } else {
            ColumnMapperPosition columnMapper = positionColumnMapper(rowType);
            return (RowMapper<Object>) rs -> columnMapper.getColumn(rs, 1);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RowMapper<T> mapper(Class<T> rowType) {
        return (RowMapper<T>) rowMappers.computeIfAbsent(rowType, this::newRowMapper);
    }
}

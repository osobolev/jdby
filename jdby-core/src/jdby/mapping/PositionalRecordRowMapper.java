package jdby.mapping;

import jdby.core.RowMapper;
import jdby.core.testing.SqlTestingHook;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class PositionalRecordRowMapper<R> implements RowMapper<R> {

    private final RecordConstructor<R> constructor;
    private final List<ColumnMapper> columnMappers;

    private PositionalRecordRowMapper(RecordConstructor<R> constructor, List<ColumnMapper> columnMappers) {
        this.constructor = constructor;
        this.columnMappers = columnMappers;
    }

    static <R> PositionalRecordRowMapper<R> create(Class<R> cls,
                                                   Function<Type, ColumnMapper> getColumnMapper) {
        RecordComponent[] rcs = Objects.requireNonNull(cls.getRecordComponents(), "Must be a record");
        List<ColumnMapper> columnMappers = new ArrayList<>(rcs.length);
        for (RecordComponent rc : rcs) {
            Type genericType = rc.getGenericType();
            ColumnMapper columnMapper = getColumnMapper.apply(genericType);
            columnMappers.add(columnMapper);
        }
        RecordConstructor<R> constructor = RecordConstructor.create(cls, rcs);
        return new PositionalRecordRowMapper<>(constructor, columnMappers);
    }

    @Override
    public R mapRow(ResultSet rs) throws SQLException {
        if (SqlTestingHook.hook != null) {
            SqlTestingHook.hook.checkRowType(rs, constructor.getDeclaringClass(), columnMappers, null);
            return null;
        }
        Object[] args = new Object[columnMappers.size()];
        for (int i = 0; i < columnMappers.size(); i++) {
            args[i] = columnMappers.get(i).getColumn(rs, 1 + i);
        }
        return constructor.newInstance(args);
    }
}

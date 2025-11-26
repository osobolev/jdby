package jdbq.mapping;

import jdbq.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

// todo: make variant for name binding instead of positional binding??? use annotations for it???
final class PositionalRecordRowMapper<R extends Record> implements RowMapper<R> {

    private final Constructor<R> constructor;
    private final List<ColumnMapperPosition> columnMappers;

    private PositionalRecordRowMapper(Constructor<R> constructor, List<ColumnMapperPosition> columnMappers) {
        this.constructor = constructor;
        this.columnMappers = columnMappers;
    }

    static <R extends Record> PositionalRecordRowMapper<R> create(Class<R> cls,
                                                                  Function<Type, ColumnMapperPosition> getColumnMapper) {
        RecordComponent[] rcs = Objects.requireNonNull(cls.getRecordComponents(), "Must be a record");
        List<ColumnMapperPosition> columnMappers = new ArrayList<>(rcs.length);
        Class<?>[] types = new Class[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            RecordComponent rc = rcs[i];
            types[i] = rc.getType();
            Type genericType = rc.getGenericType();
            ColumnMapperPosition columnMapper = getColumnMapper.apply(genericType);
            columnMappers.add(columnMapper);
        }
        Constructor<R> constructor;
        try {
            constructor = cls.getConstructor(types);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        return new PositionalRecordRowMapper<>(constructor, columnMappers);
    }

    @Override
    public R mapRow(ResultSet rs) throws SQLException {
        Object[] args = new Object[columnMappers.size()];
        for (int i = 0; i < columnMappers.size(); i++) {
            args[i] = columnMappers.get(i).getColumn(rs, 1 + i);
        }
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new SQLException(ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else {
                throw new SQLException(cause);
            }
        }
    }
}

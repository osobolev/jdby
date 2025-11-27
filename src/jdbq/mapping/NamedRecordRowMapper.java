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

final class NamedRecordRowMapper<R extends Record> implements RowMapper<R> {

    private final Constructor<R> constructor;
    private final List<NamedColumn> columns;

    private NamedRecordRowMapper(Constructor<R> constructor, List<NamedColumn> columns) {
        this.constructor = constructor;
        this.columns = columns;
    }

    static <R extends Record> NamedRecordRowMapper<R> create(Class<R> cls, ColumnNaming columnNaming,
                                                             Function<Type, ColumnMapperName> getColumnMapper) {
        RecordComponent[] rcs = Objects.requireNonNull(cls.getRecordComponents(), "Must be a record");
        List<NamedColumn> columns = new ArrayList<>(rcs.length);
        Class<?>[] types = new Class[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            RecordComponent rc = rcs[i];
            types[i] = rc.getType();
            Type genericType = rc.getGenericType();
            ColumnMapperName columnMapper = getColumnMapper.apply(genericType);
            String sqlName = columnNaming.sqlName(rc);
            columns.add(new NamedColumn(sqlName, columnMapper));
        }
        Constructor<R> constructor;
        try {
            constructor = cls.getConstructor(types);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        return new NamedRecordRowMapper<>(constructor, columns);
    }

    @Override
    public R mapRow(ResultSet rs) throws SQLException {
        Object[] args = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            NamedColumn column = columns.get(i);
            args[i] = column.mapper().getColumn(rs, column.sqlName());
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

package jdbq.mapping;

import jdbq.core.RowMapper;
import jdbq.core.testing.SqlTestingHook;

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
    private final List<ColumnMapper> columnMappers;
    private final List<String> sqlNames;

    private NamedRecordRowMapper(Constructor<R> constructor, List<ColumnMapper> columnMappers, List<String> sqlNames) {
        this.constructor = constructor;
        this.columnMappers = columnMappers;
        this.sqlNames = sqlNames;
    }

    static <R extends Record> NamedRecordRowMapper<R> create(Class<R> cls, ColumnNaming columnNaming,
                                                             Function<Type, ColumnMapper> getColumnMapper) {
        RecordComponent[] rcs = Objects.requireNonNull(cls.getRecordComponents(), "Must be a record");
        List<ColumnMapper> columnMappers = new ArrayList<>(rcs.length);
        List<String> sqlNames = new ArrayList<>(rcs.length);
        Class<?>[] types = new Class[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            RecordComponent rc = rcs[i];
            types[i] = rc.getType();
            Type genericType = rc.getGenericType();
            ColumnMapper columnMapper = getColumnMapper.apply(genericType);
            String sqlName = columnNaming.sqlName(rc);
            columnMappers.add(columnMapper);
            sqlNames.add(sqlName);
        }
        Constructor<R> constructor;
        try {
            constructor = cls.getConstructor(types);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        return new NamedRecordRowMapper<>(constructor, columnMappers, sqlNames);
    }

    @Override
    public R mapRow(ResultSet rs) throws SQLException {
        if (SqlTestingHook.hook != null) {
            SqlTestingHook.hook.checkRowType(rs, constructor.getDeclaringClass(), columnMappers, sqlNames);
            return null;
        }
        Object[] args = new Object[columnMappers.size()];
        for (int i = 0; i < columnMappers.size(); i++) {
            args[i] = columnMappers.get(i).getColumn(rs, sqlNames.get(i));
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

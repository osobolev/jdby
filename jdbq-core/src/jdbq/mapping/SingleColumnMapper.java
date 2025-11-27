package jdbq.mapping;

import jdbq.core.RowMapper;
import jdbq.core.testing.SqlTestingHook;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

final class SingleColumnMapper<T> implements RowMapper<T> {

    private final Class<T> rowType;
    private final ColumnMapper columnMapper;

    SingleColumnMapper(Class<T> rowType, ColumnMapper columnMapper) {
        this.rowType = rowType;
        this.columnMapper = columnMapper;
    }

    @Override
    public T mapRow(ResultSet rs) throws SQLException {
        Object value;
        if (SqlTestingHook.hook != null) {
            SqlTestingHook.hook.checkColumn(rs, rowType, columnMapper);
            Object array = Array.newInstance(rowType, 1);
            value = Array.get(array, 0);
        } else {
            value = columnMapper.getColumn(rs, 1);
        }
        return rowType.cast(value);
    }
}

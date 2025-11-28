package jdby.core.testing;

import jdby.mapping.ColumnMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SqlTestingHook {

    public interface Checker {

        void checkRowType(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException;

        void checkColumn(ResultSet rs, Type javaType, ColumnMapper columnMapper) throws SQLException;
    }

    public static volatile Checker hook = null;

    public static boolean isTesting() {
        return hook != null;
    }

    public static <T> T mock(Class<T> cls) {
        Object array = Array.newInstance(cls, 1);
        return cls.cast(Array.get(array, 0));
    }
}

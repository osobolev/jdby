package jdby.mapping;

import jdby.core.GeneratedKeyMapper;
import jdby.core.RowContext;
import jdby.core.testing.SqlTestingHook;

import java.lang.reflect.Type;

public interface MapperContext extends RowContext {

    ColumnMapper columnMapper(Type type);

    default <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls) {
        return generatedKey(cls, columnMapper(cls));
    }

    static <K> GeneratedKeyMapper<K> generatedKey(Class<K> cls, ColumnMapper columnMapper) {
        return (rows, columns, rs) -> {
            if (SqlTestingHook.isTesting()) {
                SqlTestingHook.hook.checkColumn(rs, cls, columnMapper);
                return SqlTestingHook.mock(cls);
            }
            if (rs.next()) {
                return cls.cast(columnMapper.getColumn(rs, 1));
            } else {
                return null;
            }
        };
    }
}

package jdbq.mapping;

import jdbq.core.GeneratedKeyMapper;
import jdbq.core.RowContext;

import java.lang.reflect.Type;

public interface MapperContext extends RowContext {

    ColumnMapper columnMapper(Type type);

    default <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls) {
        return generatedKey(cls, columnMapper(cls));
    }

    static <K> GeneratedKeyMapper<K> generatedKey(Class<K> cls, ColumnMapper columnMapper) {
        return (rows, columns, rs) -> {
            if (rs.next()) {
                return cls.cast(columnMapper.getColumn(rs, 1));
            } else {
                return null;
            }
        };
    }
}

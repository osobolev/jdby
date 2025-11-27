package jdbq.mapping;

import jdbq.core.GeneratedKeyMapper;
import jdbq.core.RowContext;

import java.lang.reflect.Type;

public interface MapperContext extends RowContext {

    ColumnMapper columnMapper(Type type);

    default <K> GeneratedKeyMapper<K> keyMapper(Class<K> cls) {
        return generatedKey(columnMapper(cls));
    }

    @SuppressWarnings("unchecked")
    static <K> GeneratedKeyMapper<K> generatedKey(ColumnMapper columnMapper) {
        return (rows, columns, rs) -> {
            if (rs.next()) {
                return (K) columnMapper.getColumn(rs, 1);
            } else {
                return null;
            }
        };
    }
}

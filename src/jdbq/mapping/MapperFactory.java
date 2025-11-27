package jdbq.mapping;

import jdbq.core.GeneratedKeyMapper;
import jdbq.core.RowMapperFactory;

import java.lang.reflect.Type;

public interface MapperFactory extends RowMapperFactory {

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

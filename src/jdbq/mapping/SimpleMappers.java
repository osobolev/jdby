package jdbq.mapping;

import jdbq.core.GeneratedKeyMapper;

// todo: remove:
public final class SimpleMappers {

    @SuppressWarnings("unchecked")
    public static <K> GeneratedKeyMapper<K> generated(ColumnMapper columnMapper) {
        return (rows, columns, rs) -> {
            if (rs.next()) {
                return (K) columnMapper.getColumn(rs, 1);
            } else {
                return null;
            }
        };
    }

    public static <K> GeneratedKeyMapper<K> generated(Class<K> cls) {
        return generated(positionColumn(cls));
    }
}

package jdby.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapper {

    Object getColumn(ResultSet rs, int index) throws SQLException;

    Object getColumn(ResultSet rs, String name) throws SQLException;

    default CheckColumnCompatibility checkCompatibility() {
        return null;
    }

    static ColumnMapper jdbcMapper(Class<?> cls) {
        return new SimpleColumnMapper<>(
            (rs, index) -> rs.getObject(index, cls),
            (rs, name) -> rs.getObject(name, cls)
        );
    }
}

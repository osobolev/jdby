package jdby.core;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class SimpleSqlParameter implements SqlParameter {

    private final Object value;
    private final JDBCType type;

    SimpleSqlParameter(Object value, JDBCType type) {
        this.value = value;
        this.type = type;
    }

    public void set(PreparedStatement ps, int index) throws SQLException {
        ps.setObject(index, value, type.getVendorTypeNumber().intValue());
    }
}

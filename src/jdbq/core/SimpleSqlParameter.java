package jdbq.core;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class SimpleSqlParameter implements SqlParameter {

    private final Object value;
    private final JDBCType type;

    public SimpleSqlParameter(Object value, JDBCType type) {
        this.value = value;
        this.type = type;
    }

    public void set(PreparedStatement ps, int index) throws SQLException {
        ps.setObject(index, value, type.getVendorTypeNumber().intValue());
    }
}

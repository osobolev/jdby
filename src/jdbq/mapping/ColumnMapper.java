package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapper {

    Object getColumn(ResultSet rs, int index) throws SQLException;

    Object getColumn(ResultSet rs, String name) throws SQLException;

    default CheckColumnCompatibility checkCompatibility() {
        return null;
    }
}

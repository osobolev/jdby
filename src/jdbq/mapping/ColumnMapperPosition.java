package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapperPosition {

    Object getColumn(ResultSet rs, int index) throws SQLException;
}

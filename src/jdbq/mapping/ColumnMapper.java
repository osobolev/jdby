package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapper {

    Object getColumn(ResultSet rs, int index) throws SQLException;
}

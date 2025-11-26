package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapperName {

    Object getColumn(ResultSet rs, String name) throws SQLException;
}

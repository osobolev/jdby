package jdbq.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface GeneratedKeyMapper<T> {

    T map(int rows, String[] columns, ResultSet rs) throws SQLException;
}

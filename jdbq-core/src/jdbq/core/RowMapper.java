package jdbq.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface RowMapper<T> {

    T mapRow(ResultSet rs) throws SQLException;

    default List<T> mapAllRows(ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
}

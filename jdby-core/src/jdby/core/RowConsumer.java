package jdby.core;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowConsumer {

    void consumeRow(ResultSet rs) throws SQLException;
}

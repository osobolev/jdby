package jdbq.core;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlTransaction {

    Connection getConnection() throws SQLException;
}

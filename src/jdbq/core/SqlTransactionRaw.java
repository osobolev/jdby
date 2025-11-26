package jdbq.core;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlTransactionRaw {

    Connection getConnection() throws SQLException;
}

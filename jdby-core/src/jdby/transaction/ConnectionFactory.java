package jdby.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {

    Connection openConnection() throws SQLException;

    default void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    static ConnectionFactory fromConnection(Connection connection) {
        return new ConnectionFactory() {

            @Override
            public Connection openConnection() {
                return connection;
            }

            @Override
            public void closeConnection(Connection connection) {
                // do nothing
            }
        };
    }

    static ConnectionFactory fromDataSource(DataSource dataSource) {
        return dataSource::getConnection;
    }
}

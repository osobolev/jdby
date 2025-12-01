package jdby.core;

import jdby.internal.RollbackGuard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public interface ConnectionFactory {

    Connection openConnection() throws SQLException;

    default void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    interface SqlFunction<C, R, E extends Exception> {

        R call(C connection) throws E;
    }

    static <C, R, E extends Exception> R transactionCall(ConnectionFactory dataSource,
                                                         Function<Connection, C> wrap,
                                                         SqlFunction<C, R, E> function) throws E {
        try (RollbackGuard guard = RollbackGuard.create(dataSource)) {
            C connection = wrap.apply(guard.getConnection());
            R result = function.call(connection);
            guard.ok();
            return result;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    interface SqlAction<C, E extends Exception> {

        void run(C connection) throws E;
    }

    static <C, E extends Exception> void transactionAction(ConnectionFactory dataSource,
                                                           Function<Connection, C> wrap,
                                                           SqlAction<C, E> action) throws E {
        transactionCall(dataSource, wrap, connection -> {
            action.run(connection);
            return null;
        });
    }
}

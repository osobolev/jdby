package jdby.transaction;

import jdby.core.UncheckedSQLException;
import jdby.internal.RollbackGuard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public final class SqlTransaction<C> {

    private final ConnectionFactory dataSource;
    private final Function<Connection, C> wrap;

    public SqlTransaction(ConnectionFactory dataSource, Function<Connection, C> wrap) {
        this.dataSource = dataSource;
        this.wrap = wrap;
    }

    public <R, E extends Exception> R call(SqlFunction<C, R, E> function) throws E {
        try (RollbackGuard guard = RollbackGuard.create(dataSource)) {
            C connection = wrap.apply(guard.getConnection());
            R result = function.call(connection);
            guard.ok();
            return result;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public <E extends Exception> void run(SqlAction<C, E> action) throws E {
        call(connection -> {
            action.run(connection);
            return null;
        });
    }
}

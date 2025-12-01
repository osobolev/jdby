package jdby.testing;

import jdby.dao.DaoContext;
import jdby.dao.DefaultDaoContextBuilder;
import jdby.mapping.CheckColumnCompatibility;
import jdby.mapping.ColumnNaming;

import java.sql.Connection;
import java.util.function.Consumer;

public final class TestingOptions {

    public interface ConnectionInit {

        void start(Connection connection) throws Exception;
    }

    /**
     * Is called for each column, if not null. Can:
     * <ul>
     *     <li>Whitelist a column type returning true</li>
     *     <li>Blacklist a column type returning false</li>
     *     <li>Allow standard column type handling returning null</li>
     * </ul>
     */
    public CheckColumnCompatibility checkColumns = null;
    /**
     * Can be overriden to support custom parameter types
     */
    public MockParamFactory paramFactory = new MockParamFactory();
    /**
     * If {@link ColumnNaming} created in {@link #instantiator} is {@link jdby.mapping.ColumnNaming.ByPosition}, then
     * java field names are compared with DB column names at the same indexes. Normalized names are compared
     * (lower case with underscores removed). When there are differences, warning is emitted.
     */
    public boolean checkNamesForPositions = true;
    /**
     * See {@link TestStrictness}
     */
    public TestStrictness strictness = TestStrictness.STRICT_TYPE_CHECK;
    public DefaultDaoContextBuilder ctx = DaoContext.builder();
    public TestInstantiator instantiator = new DefaultTestInstantiator(ctx);
    public ConnectionInit initConnection = connection -> {};

    public Consumer<String> info = System.out::println;
    public Consumer<String> warn = System.err::println;
    public Consumer<String> error = message -> {
        throw new IllegalStateException(message);
    };

    public void info(String message) {
        info.accept(message);
    }

    public void warn(String message) {
        warn.accept(message);
    }

    public void error(String message) {
        error.accept(message);
    }
}

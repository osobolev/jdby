package jdbq.testing;

import jdbq.core.RowContext;
import jdbq.dao.DefaultDaoContext;
import jdbq.mapping.CheckColumnCompatibility;
import jdbq.mapping.ColumnNaming;

import java.util.function.Consumer;

public final class TestingOptions {

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
     * If {@link #ctx}'s {@link ColumnNaming} is {@link jdbq.mapping.ColumnNaming.ByPosition}, then
     * java field names are compared with DB column names at the same indexes. Normalized names are compared
     * (lower case with underscores removed). When there are differences, warning is emitted.
     */
    public boolean checkNamesForPositions = true;
    /**
     * See {@link TestStrictness}
     */
    public TestStrictness strictness = TestStrictness.STRICT_TYPE_CHECK;
    public RowContext ctx = new DefaultDaoContext(ColumnNaming.camelCase());

    public Consumer<String> info = System.out::println;
    public Consumer<String> warn = System.err::println;
    public Consumer<String> error = message -> {
        throw new IllegalArgumentException(message);
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

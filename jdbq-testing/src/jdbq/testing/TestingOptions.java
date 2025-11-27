package jdbq.testing;

import jdbq.core.RowContext;
import jdbq.dao.DefaultDaoContext;
import jdbq.mapping.CheckColumnCompatibility;
import jdbq.mapping.ColumnNaming;

import java.util.function.Consumer;

public final class TestingOptions {

    public CheckColumnCompatibility check;
    public Consumer<String> info = System.out::println;
    public Consumer<String> warn = System.err::println;
    public Consumer<String> error = message -> {
        throw new IllegalArgumentException(message);
    };
    public boolean checkNamesForPositions = true;
    public TestStrictness strictness = TestStrictness.STRICT_TYPE_CHECK;
    public RowContext ctx = new DefaultDaoContext(ColumnNaming.camelCase());

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

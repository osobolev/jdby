package jdbq.testing;

import jdbq.mapping.CheckColumnCompatibility;

import java.util.function.Consumer;

public final class TestingOptions {

    public CheckColumnCompatibility check;
    public Consumer<String> warn = System.err::println;
    public Consumer<String> error = message -> {
        throw new IllegalArgumentException(message);
    };
    public boolean checkNamesForPositions = true;

    public void warn(String message) {
        warn.accept(message);
    }

    public void error(String message) {
        error.accept(message);
    }
}

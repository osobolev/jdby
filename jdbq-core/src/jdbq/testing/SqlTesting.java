package jdbq.testing;

public final class SqlTesting {

    public static volatile TestingHook testing = null;

    public static boolean isTesting() {
        return testing != null;
    }
}

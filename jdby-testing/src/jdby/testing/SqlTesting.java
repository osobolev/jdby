package jdby.testing;

import jdby.core.testing.SqlTestingHook;
import jdby.internal.RollbackGuard;
import jdby.internal.Utils;
import jdby.transaction.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

public final class SqlTesting {

    private final TestingOptions options;
    private final Connection connection;

    private SqlTesting(TestingOptions options, Connection connection) {
        this.options = options;
        this.connection = connection;
    }

    private void runAllMethods(Class<?> cls, Object o) throws Exception {
        ConnectionFactory testConnectionFactory = ConnectionFactory.fromConnection(connection);
        for (Method method : cls.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers))
                continue;
            if (Modifier.isStatic(modifiers))
                continue;
            options.info("\t" + method.getName());
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Type type = param.getParameterizedType();
                args[i] = options.paramFactory.mockParameter(type, () -> {
                    throw new IllegalStateException(String.format(
                        "Unsupported type '%s' of parameter '%s' of method %s",
                        type.getTypeName(), param.getName(), Utils.methodString(method)
                    ));
                });
            }
            try (RollbackGuard guard = RollbackGuard.create(testConnectionFactory)) {
                method.invoke(o, args);
            } catch (InvocationTargetException itex) {
                if (itex.getCause() instanceof Exception ex) {
                    throw ex;
                } else {
                    throw itex;
                }
            }
        }
    }

    public interface CreateTestDao {

        Object newDao(Connection connection, Class<?> cls) throws Exception;
    }

    public static void runTests(TestingOptions options, Callable<Connection> getConnection,
                                List<Class<?>> daoClasses, CreateTestDao factory) throws Exception {
        try (Connection connection = getConnection.call()) {
            connection.setAutoCommit(false);
            options.initConnection.start(connection);
            SqlTestingHook.hook = new TestingHookImpl(options);
            SqlTesting testing = new SqlTesting(options, connection);
            for (Class<?> daoClass : daoClasses) {
                options.info("Running " + daoClass.getName());
                Object instance = factory.newDao(connection, daoClass);
                testing.runAllMethods(daoClass, instance);
            }
        }
    }

    public static void runTests(TestingOptions options, Callable<Connection> getConnection,
                                List<Class<?>> daoClasses) throws Exception {
        runTests(options, getConnection, daoClasses, options.instantiator::create);
    }

    /**
     * @param file properties file with keys {@code jdbc.url}, {@code jdbc.username}, {@code jdbc.password}
     */
    public static Connection fromFile(Path file) throws IOException, SQLException {
        Properties props = new Properties();
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            props.load(rdr);
        }
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * {@link #fromFile(Path)} with default file {@code db.properties}
     */
    public static Connection fromFile() throws IOException, SQLException {
        return fromFile(Path.of("db.properties"));
    }
}

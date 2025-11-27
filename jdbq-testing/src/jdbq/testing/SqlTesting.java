package jdbq.testing;

import jdbq.core.RowConnection;
import jdbq.core.RowContext;
import jdbq.core.testing.SqlTestingHook;
import jdbq.dao.DaoConnection;
import jdbq.dao.DaoContext;
import jdbq.dao.DaoSql;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class SqlTesting {

    private final TestingOptions options;
    private final Connection connection;

    private SqlTesting(TestingOptions options, Connection connection) {
        this.options = options;
        this.connection = connection;
    }

    private Object mockObject(Type type, Supplier<RuntimeException> onFail) {
        if (type == int.class || type == Integer.class) {
            return 1;
        } else if (type == long.class || type == Long.class) {
            return 1L;
        } else if (type == double.class || type == Double.class) {
            return 1.0;
        } else if (type == byte.class || type == Byte.class) {
            return (byte) 1;
        } else if (type == short.class || type == Short.class) {
            return (short) 1;
        } else if (type == float.class || type == Float.class) {
            return 1.0f;
        } else if (type == BigDecimal.class) {
            return BigDecimal.ONE;
        } else if (type == String.class) {
            return "1";
        } else if (type == LocalDate.class) {
            return LocalDate.now();
        } else if (type == OffsetDateTime.class) {
            return OffsetDateTime.now();
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (type == LocalTime.class) {
            return LocalTime.now();
        } else if (type == byte[].class) {
            return new byte[] {1};
        } else if (type == boolean.class || type == Boolean.class) {
            return true;
        } else {
            // todo: custom type as well!!!
            throw onFail.get();
        }
    }

    private static final class RollbackGuard implements AutoCloseable {

        private final Connection realConnection;

        RollbackGuard(Connection realConnection) {
            this.realConnection = realConnection;
        }

        @Override
        public void close() throws SQLException {
            realConnection.rollback();
        }
    }

    private void runAllMethods(Class<?> cls, Object o) throws Throwable {
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
                int paramIndex = i;
                Type type = param.getParameterizedType();
                args[i] = mockObject(type, () -> {
                    String arg = param.getName() == null ? String.valueOf(paramIndex + 1) : param.getName();
                    return new IllegalStateException(String.format("Unsupported arg type '%s' for arg %s", type, arg));
                });
            }
            try (RollbackGuard guard = new RollbackGuard(connection)) {
                method.invoke(o, args);
            } catch (InvocationTargetException itex) {
                throw itex.getTargetException();
            }
        }
    }

    public interface CreateTestDao {

        Object newDao(Connection connection, Class<?> cls) throws Exception;
    }

    public static void runTests(TestingOptions options, Callable<Connection> getConnection,
                                List<Class<?>> daoClasses, CreateTestDao factory) throws Throwable {
        try (Connection connection = getConnection.call()) {
            connection.setAutoCommit(false);
            SqlTestingHook.hook = new TestingHookImpl(options);
            SqlTesting testing = new SqlTesting(options, connection);
            for (Class<?> daoClass : daoClasses) {
                options.info("Running " + daoClass.getSimpleName());
                Object instance = factory.newDao(connection, daoClass);
                testing.runAllMethods(daoClass, instance);
            }
        }
    }

    private static boolean match(RowContext ctx, Class<?> paramType) {
        if (paramType == Connection.class) {
            return true;
        } else if (paramType == RowConnection.class) {
            return ctx != null;
        } else if (paramType == DaoConnection.class) {
            return ctx instanceof DaoContext;
        } else {
            return false;
        }
    }

    private static Object createTestDao(RowContext ctx, Class<?> cls, Connection connection) throws Exception {
        Constructor<?>[] constructors = cls.getConstructors();
        List<Constructor<?>> candidates0 = new ArrayList<>();
        List<Constructor<?>> candidates1 = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 0) {
                candidates0.add(constructor);
            } else if (types.length == 1) {
                Class<?> type = types[0];
                if (match(ctx, type)) {
                    candidates1.add(constructor);
                }
            }
        }
        if (candidates1.size() == 1) {
            Constructor<?> constructor = candidates1.get(0);
            Class<?> type = constructor.getParameterTypes()[0];
            if (type == Connection.class) {
                return constructor.newInstance(connection);
            } else {
                return constructor.newInstance(ctx.withConnection(connection));
            }
        } else if (candidates0.size() == 1) {
            Constructor<?> constructor = candidates0.get(0);
            return constructor.newInstance();
        } else {
            throw new IllegalArgumentException("Cannot find constructor for " + cls);
        }
    }

    public static void runTests(TestingOptions options, Callable<Connection> getConnection,
                                List<Class<?>> daoClasses) throws Throwable {
        runTests(
            options, getConnection, daoClasses,
            (connection, cls) -> createTestDao(options.ctx, cls, connection)
        );
    }

    public static Connection fromProperties(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static Connection fromFile(Path file) throws IOException, SQLException {
        Properties props = new Properties();
        try (BufferedReader rdr = Files.newBufferedReader(file)) {
            props.load(rdr);
        }
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return fromProperties(url, username, password);
    }

    public static Connection fromFile() throws IOException, SQLException {
        return fromFile(Path.of("db.properties"));
    }
}

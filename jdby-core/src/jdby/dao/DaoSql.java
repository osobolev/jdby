package jdby.dao;

import jdby.core.Query;
import jdby.core.RowConnection;
import jdby.core.SqlParameter;
import jdby.internal.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DaoSql {

    private static final ThreadLocal<CallData> CALL_DATA = new ThreadLocal<>();

    public static <T> T createProxy(DaoContext ctx, Class<T> iface, Connection connection) {
        Object created = Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class<?>[] {iface},
            (proxy, method, args) -> runProxyMethod(
                ctx, connection, iface, proxy, method, args
            )
        );
        return iface.cast(created);
    }

    private static Object runProxyMethod(DaoContext ctx, Connection connection,
                                         Class<?> iface, Object proxy, Method method, Object[] args) throws Throwable {
        Parameter[] parameters = method.getParameters();
        String name = method.getName();
        if ("toString".equals(name) && parameters.length == 0) {
            return "<proxy for " + iface.getName() + ">";
        } else if ("hashCode".equals(name) && parameters.length == 0) {
            return proxy.hashCode();
        } else if ("equals".equals(name) && parameters.length == 1 && parameters[0].getType() == Object.class) {
            return proxy.equals(args[0]);
        }
        Map<String, SqlParameter> argsMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isNamePresent()) {
                throw new IllegalArgumentException("Parameter names are not present for interface '" + iface.getName() + "'; recompile with '-parameters'");
            }
            SqlParameter value = ctx.parameter(parameter.getParameterizedType(), args[i]);
            argsMap.put(parameter.getName(), value);
        }
        if (!method.isDefault()) {
            throw new IllegalArgumentException("Call of non-default method " + Utils.methodString(method));
        }
        if (CALL_DATA.get() != null) {
            throw new IllegalStateException("Cannot call proxy from proxy");
        }
        CALL_DATA.set(new CallData(ctx, method, argsMap, connection));
        try {
            return InvocationHandler.invokeDefault(proxy, method, args);
        } finally {
            CALL_DATA.remove();
        }
    }

    private static CallData getCallData() {
        CallData data = CALL_DATA.get();
        return Objects.requireNonNull(data, "Must call through the proxy");
    }

    public static void parameter(String name, SqlParameter value) {
        CallData data = getCallData();
        data.parameters.put(name, value);
    }

    public static <T> void parameter(String name, T value, Class<T> cls) {
        CallData data = getCallData();
        data.parameters.put(name, data.ctx.parameter(cls, value));
    }

    public static RowConnection getConnection() {
        CallData data = getCallData();
        return data.ctx.withConnection(data.connection);
    }

    public static SqlBuilder builder(String... sql) {
        SqlBuilder buf = new SqlBuilder();
        for (String s : sql) {
            buf.append(s);
        }
        return buf;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listRows(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (List<T>) query.listRows(data.connection, data.rowMapper(List.class));
    }

    @SuppressWarnings("unchecked")
    public static <T> T exactlyOneRow(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.exactlyOneRow(data.connection, data.rowMapper(null));
    }

    @SuppressWarnings("unchecked")
    public static <T> T maybeRow(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.maybeRow(data.connection, data.rowMapper(null));
    }

    public static int executeUpdate(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return query.executeUpdate(data.connection);
    }

    @SuppressWarnings("unchecked")
    public static <T> T executeUpdate(CharSequence sql, String generatedColumn, String... otherGeneratedColumns) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.executeUpdate(data.connection, data.keyMapper(), generatedColumn, otherGeneratedColumns);
    }
}

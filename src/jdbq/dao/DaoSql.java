package jdbq.dao;

import jdbq.core.Query;
import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DaoSql {

    private static ThreadLocal<CallData> callData = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(DaoContext ctx, Class<T> iface, SqlTransactionRaw getConnection) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class<?>[] {iface},
            (proxy, method, args) -> runProxyMethod(
                ctx, getConnection, iface, proxy, method, args
            )
        );
    }

    private static Object runProxyMethod(DaoContext ctx, SqlTransactionRaw t,
                                         Class<?> iface, Object proxy, Method method, Object[] args) throws Throwable {
        Parameter[] parameters = method.getParameters();
        String name = method.getName();
        if ("toString".equals(name) && parameters.length == 0) {
            return "<proxy for " + iface.getName() + ">";
        } else if ("hashCode".equals(name) && parameters.length == 0) {
            return proxy.hashCode();
        } else if ("equals".equals(name) && parameters.length == 1 && parameters[0].getType() == Object.class) {
            return proxy == args[0];
        }
        Map<String, SqlParameter> argsMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isNamePresent()) {
                throw new IllegalArgumentException("Parameter name is not present for " + method);
            }
            SqlParameter value = ctx.parameter(parameter.getType(), args[i]);
            argsMap.put(parameter.getName(), value);
        }
        if (!method.isDefault()) {
            throw new IllegalArgumentException("Call to non-default method " + method);
        }
        try {
            callData.set(new CallData(ctx, method, argsMap, t));
            return InvocationHandler.invokeDefault(proxy, method, args);
        } finally {
            callData.remove();
        }
    }

    private static CallData getCallData() {
        CallData data = callData.get();
        return Objects.requireNonNull(data, "Must call through the proxy");
    }

    public static Query piece(String sql) {
        CallData data = getCallData();
        return data.substituteArgs(sql);
    }

    public static void parameter(String name, SqlParameter value) {
        CallData data = getCallData();
        data.parameters.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listRows(String sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (List<T>) query.listRows(data.t, data.mapper());
    }

    @SuppressWarnings("unchecked")
    public static <T> T exactlyOneRow(String sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.exactlyOneRow(data.t, data.mapper());
    }

    @SuppressWarnings("unchecked")
    public static <T> T maybeRow(String sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.maybeRow(data.t, data.mapper());
    }

    // todo: other Query methods too!!!
}

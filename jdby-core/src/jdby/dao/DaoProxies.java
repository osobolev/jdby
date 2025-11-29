package jdby.dao;

import jdby.core.SqlParameter;
import jdby.internal.Utils;

import java.lang.reflect.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public final class DaoProxies {

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
            Type type = parameter.getParameterizedType();
            Object arg = args[i];
            if (ctx.nonSqlParameter(type, arg))
                continue;
            if (!parameter.isNamePresent()) {
                throw new IllegalArgumentException("Parameter names are not present for interface '" + iface.getName() + "'; recompile with '-parameters'");
            }
            SqlParameter value = ctx.parameter(type, arg);
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

    static CallData getCallData() {
        CallData data = CALL_DATA.get();
        if (data == null) {
            throw new IllegalStateException("Must call through the proxy");
        }
        return data;
    }
}

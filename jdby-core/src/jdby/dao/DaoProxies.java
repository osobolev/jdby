package jdby.dao;

import jdby.core.SqlParameter;
import jdby.internal.Utils;

import java.lang.reflect.*;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public final class DaoProxies {

    private static final ThreadLocal<ArrayDeque<CallData>> CALL_DATA = new ThreadLocal<>();

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
                throw new IllegalStateException("Parameter names are not present for interface '" + iface.getName() + "'; recompile with '-parameters'");
            }
            SqlParameter value = ctx.parameter(type, arg);
            argsMap.put(parameter.getName(), value);
        }
        if (!method.isDefault()) {
            throw new IllegalStateException("Call of non-default method " + Utils.methodString(method));
        }
        ArrayDeque<CallData> deque = CALL_DATA.get();
        if (deque == null) {
            deque = new ArrayDeque<>();
            CALL_DATA.set(deque);
        }
        deque.push(new CallData(ctx, method, argsMap, connection));
        try {
            return InvocationHandler.invokeDefault(proxy, method, args);
        } finally {
            deque.pop();
            if (deque.isEmpty()) {
                CALL_DATA.remove();
            }
        }
    }

    static CallData getCallData() {
        ArrayDeque<CallData> deque = CALL_DATA.get();
        CallData data = deque == null ? null : deque.peek();
        if (data == null) {
            throw new IllegalStateException("Must call through the proxy");
        }
        return data;
    }
}

package jdbq.dao;

import jdbq.core.Query;
import jdbq.core.RowMapperFactory;
import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo: rename???
public final class ProxyQueries {

    private static ThreadLocal<Map<String, SqlParameter>> savedArgsCell = new ThreadLocal<>();
    private static ThreadLocal<RowMapperFactory> rowMapperFactoryCell = new ThreadLocal<>();
    private static ThreadLocal<Class<?>> rowTypeCell = new ThreadLocal<>();
    private static ThreadLocal<SqlTransactionRaw> connectionCell = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    public static <T> T create(DaoContext ctx, Class<T> cls, SqlTransactionRaw getConnection) {
        return (T) Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] {cls},
            (proxy, method, args) -> runProxyMethod(
                ctx, getConnection, cls, proxy, method, args
            )
        );
    }

    private static Class<?> getRowType(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> cls) {
            return cls;
        } else if (returnType instanceof ParameterizedType pt) {
            Type[] typeArguments = pt.getActualTypeArguments();
            if (typeArguments.length != 1) {
                throw new IllegalArgumentException("Method " + method + " return type must have only 1 generic parameter");
            }
            if (typeArguments[0] instanceof Class<?> cls) {
                return cls;
            } else {
                throw new IllegalArgumentException("Method " + method + " return type generic parameter must be a class");
            }
        } else {
            throw new IllegalArgumentException("Method " + method + " return type must be a class or a simple generic");
        }
    }

    private static Object runProxyMethod(DaoContext ctx, SqlTransactionRaw t,
                                         Class<?> cls, Object proxy, Method method, Object[] args) throws Throwable {
        // todo: better check for method signatures!!!
        if ("sqlPiece".equals(method.getName())) {
            ParsedQuery pq = ParsedQuery.parse((String) args[0]);
            List<SqlParameter> params = new ArrayList<>();
            for (String paramName : pq.paramNames) {
                SqlParameter value = savedArgsCell.get().get(paramName); // todo: check that globals are set!!!
                if (value == null)
                    throw new IllegalArgumentException("Parameter " + paramName + " is not defined");
                params.add(value);
            }
            return new Query(pq.sql, params);
        } else if ("toString".equals(method.getName())) {
            return "<proxy for " + cls.getName() + ">";
        }
        // todo: equals/hashCode too!!!
        Parameter[] parameters = method.getParameters();
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
        Class<?> rowType = getRowType(method);
        try {
            savedArgsCell.set(argsMap);
            connectionCell.set(t);
            rowMapperFactoryCell.set(ctx);
            rowTypeCell.set(rowType);
            return InvocationHandler.invokeDefault(proxy, method, args);
        } finally {
            savedArgsCell.remove();
            connectionCell.remove();
            rowMapperFactoryCell.remove();
            rowTypeCell.remove();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listRows(String sql) throws SQLException {
        Map<String, SqlParameter> args = savedArgsCell.get();
        SqlTransactionRaw t = connectionCell.get();
        RowMapperFactory rowMapperFactory = rowMapperFactoryCell.get();
        Class<?> rowType = rowTypeCell.get();
        // todo: check that globals are set!!!
        ParsedQuery pq = ParsedQuery.parse(sql); // todo: cache it???
        Query query = pq.toQuery(args::get);
        return (List<T>) query.listRows(t, rowMapperFactory.mapper(rowType));
    }

    // todo: other Query methods too!!!
    // todo: add method to create Query too!!!
    // todo: add method to add artificial parameter!!!
}

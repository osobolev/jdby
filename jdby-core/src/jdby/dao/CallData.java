package jdby.dao;

import jdby.core.GeneratedKeyMapper;
import jdby.core.Query;
import jdby.core.RowMapper;
import jdby.core.SqlParameter;
import jdby.internal.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

final class CallData {

    final DaoContext ctx;
    final Method method;
    final Map<String, SqlParameter> parameters;
    final Connection connection;

    CallData(DaoContext ctx, Method method, Map<String, SqlParameter> parameters, Connection connection) {
        this.ctx = ctx;
        this.method = method;
        this.parameters = parameters;
        this.connection = connection;
    }

    Query substituteArgs(CharSequence sql) {
        ParsedQuery pq = ParsedQuery.parse(sql); // todo: cache it???
        return pq.toQuery(parameters);
    }

    RowMapper<?> rowMapper(boolean list) {
        return ctx.rowMapper(getRowType(list));
    }

    GeneratedKeyMapper<?> keyMapper() {
        return ctx.keyMapper(getRowType(false));
    }

    private Class<?> getRowType(boolean list) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> cls) {
            return cls;
        }
        if (!list) {
            throw new IllegalStateException("Method " + Utils.methodString(method) + " return type must be non-generic");
        } else {
            Class<?> paramClass = getGenericParameter(returnType, List.class);
            if (paramClass != null)
                return paramClass;
            throw new IllegalStateException("Method " + Utils.methodString(method) + " return type must be 'List<...>'");
        }
    }

    private static Class<?> getGenericParameter(Type type, Class<?> requiredClass) {
        if (type instanceof ParameterizedType pt && pt.getRawType() == requiredClass) {
            Type[] typeArguments = pt.getActualTypeArguments();
            if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> cls) {
                return cls;
            }
        }
        return null;
    }
}

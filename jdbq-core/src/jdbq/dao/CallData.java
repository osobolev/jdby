package jdbq.dao;

import jdbq.core.GeneratedKeyMapper;
import jdbq.core.Query;
import jdbq.core.RowMapper;
import jdbq.core.SqlParameter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
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
        return pq.toQuery(parameters::get);
    }

    RowMapper<?> rowMapper(Class<?> requiredClass) {
        return ctx.rowMapper(getRowType(requiredClass));
    }

    GeneratedKeyMapper<?> keyMapper() {
        return ctx.keyMapper(getRowType(null));
    }

    private Class<?> getRowType(Class<?> requiredClass) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> cls) {
            return cls;
        }
        if (requiredClass == null) {
            throw new IllegalArgumentException("Method " + DaoSql.methodString(method) + " return type must be non-generic");
        } else {
            Class<?> paramClass = getGenericParameter(returnType, requiredClass);
            if (paramClass != null)
                return paramClass;
            throw new IllegalArgumentException("Method " + DaoSql.methodString(method) + " return type must be " + requiredClass.getName() + "<...>");
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

package jdbq.dao;

import jdbq.core.*;

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

    Query substituteArgs(String sql) {
        ParsedQuery pq = ParsedQuery.parse(sql); // todo: cache it???
        return pq.toQuery(parameters::get);
    }

    RowMapper<?> rowMapper() {
        return ctx.rowMapper(getRowType());
    }

    GeneratedKeyMapper<?> keyMapper() {
        return ctx.keyMapper(getRowType());
    }

    private Class<?> getRowType() {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> cls) {
            return cls;
        } else if (returnType instanceof ParameterizedType pt) {
            Type[] typeArguments = pt.getActualTypeArguments();
            if (typeArguments.length != 1) {
                throw new IllegalArgumentException("Method '" + method + "' return type must have only 1 generic parameter");
            }
            if (typeArguments[0] instanceof Class<?> cls) {
                return cls;
            } else {
                throw new IllegalArgumentException("Method '" + method + "' return type generic parameter must be a class");
            }
        } else {
            throw new IllegalArgumentException("Method '" + method + "' return type must be a class or a simple generic");
        }
    }
}

package jdbq.dao;

import jdbq.core.Query;
import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

final class CallData {

    final DaoContext ctx;
    final Method method;
    final Map<String, SqlParameter> parameters;
    final SqlTransactionRaw t;

    CallData(DaoContext ctx, Method method, Map<String, SqlParameter> parameters, SqlTransactionRaw t) {
        this.ctx = ctx;
        this.method = method;
        this.parameters = parameters;
        this.t = t;
    }

    Query substituteArgs(String sql) {
        ParsedQuery pq = ParsedQuery.parse(sql); // todo: cache it???
        return pq.toQuery(parameters::get);
    }

    Class<?> getRowType() {
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
}

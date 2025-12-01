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
import java.util.Optional;

final class CallData {

    static final int OPTIONAL = 0;
    static final int ONE = 1;
    static final int LIST = 2;

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

    RowMapper<?> rowMapper(int multiplicity) {
        return ctx.getMapperContext().rowMapper(getRowType(multiplicity));
    }

    GeneratedKeyMapper<?> keyMapper() {
        return ctx.getMapperContext().keyMapper(getRowType(ONE));
    }

    @SuppressWarnings("unchecked")
    <T> T cast(Object value) {
        if (unwrapType(Optional.class) != null) {
            return (T) Optional.ofNullable(value);
        } else {
            return (T) value;
        }
    }

    private Class<?> getRowType(int multiplicity) {
        if (multiplicity != LIST) {
            Class<?> plainType = unwrapType(null);
            if (plainType != null) {
                return plainType;
            }
            if (multiplicity == OPTIONAL) {
                Class<?> rowType = unwrapType(Optional.class);
                if (rowType != null)
                    return rowType;
                throw new IllegalStateException("Method " + Utils.methodString(method) + " return type must be a simple class or Optional<...>");
            } else {
                throw new IllegalStateException("Method " + Utils.methodString(method) + " return type must be a simple class");
            }
        } else {
            Class<?> rowType = unwrapType(List.class);
            if (rowType != null)
                return rowType;
            throw new IllegalStateException("Method " + Utils.methodString(method) + " return type must be 'List<...>'");
        }
    }

    private Class<?> unwrapType(Class<?> wrapperClass) {
        Type type = method.getGenericReturnType();
        if (wrapperClass == null) {
            if (type instanceof Class<?> cls) {
                return cls;
            }
        } else {
            if (type instanceof ParameterizedType pt && pt.getRawType() == wrapperClass) {
                Type[] typeArguments = pt.getActualTypeArguments();
                if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> cls) {
                    return cls;
                }
            }
        }
        return null;
    }
}

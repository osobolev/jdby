package jdby.mapping;

import jdby.core.UncheckedSQLException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLException;

final class RecordConstructor<R> {

    private final Constructor<R> constructor;

    private RecordConstructor(Constructor<R> constructor) {
        this.constructor = constructor;
    }

    static <R> RecordConstructor<R> create(Class<R> cls, RecordComponent[] rcs) {
        Class<?>[] types = new Class[rcs.length];
        for (int i = 0; i < rcs.length; i++) {
            types[i] = rcs[i].getType();
        }
        try {
            return new RecordConstructor<>(cls.getConstructor(types));
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    Class<R> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }

    R newInstance(Object[] args) throws SQLException {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException sex) {
                throw sex;
            } else if (cause instanceof RuntimeException rtex) {
                throw rtex;
            } else {
                throw new UncheckedSQLException(cause);
            }
        }
    }
}

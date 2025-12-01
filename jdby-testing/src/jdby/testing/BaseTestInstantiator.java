package jdby.testing;

import jdby.core.RowConnection;
import jdby.dao.DaoConnection;
import jdby.dao.DaoContext;
import jdby.dao.DaoProxies;
import jdby.mapping.MapperContext;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class BaseTestInstantiator implements TestInstantiator {

    protected abstract MapperContext mapper();

    protected abstract DaoContext dao();

    private <T> Callable<T> match1(Connection connection, Constructor<T> constructor1, Class<?> paramType) {
        if (paramType == Connection.class) {
            return () -> constructor1.newInstance(connection);
        } else if (paramType == DaoConnection.class) {
            return () -> constructor1.newInstance(dao().withConnection(connection));
        } else if (paramType == RowConnection.class) {
            return () -> constructor1.newInstance(mapper().withConnection(connection));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Connection connection, Class<T> cls) throws Exception {
        if (cls.isInterface()) {
            return DaoProxies.createProxy(dao(), connection, cls);
        }
        Constructor<?>[] constructors = cls.getConstructors();
        List<Callable<T>> candidates0 = new ArrayList<>();
        List<Callable<T>> candidates1 = new ArrayList<>();
        for (Constructor<?> rawConstructor : constructors) {
            Constructor<T> constructor = (Constructor<T>) rawConstructor;
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 0) {
                candidates0.add(constructor::newInstance);
            } else if (types.length == 1) {
                Class<?> type = types[0];
                Callable<T> newInstance = match1(connection, constructor, type);
                if (newInstance != null) {
                    candidates1.add(newInstance);
                }
            }
        }
        if (candidates1.size() == 1) {
            Callable<T> newInstance = candidates1.get(0);
            return newInstance.call();
        } else if (candidates0.size() == 1) {
            Callable<T> newInstance = candidates0.get(0);
            return newInstance.call();
        }
        throw new IllegalArgumentException("Cannot find appropriate constructor for class '" + cls.getName() + "'");
    }
}

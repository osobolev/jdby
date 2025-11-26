package jdbq.dao;

import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;
import jdbq.mapping.MapperFactory;

import java.lang.reflect.Type;

public interface DaoContext extends MapperFactory {

    SqlParameter parameter(Type type, Object value);

    // todo: remove???
    <T extends BaseDao> T proxy(Class<T> cls, SqlTransactionRaw t);
}

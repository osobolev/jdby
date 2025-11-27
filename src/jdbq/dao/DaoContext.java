package jdbq.dao;

import jdbq.core.SqlParameter;
import jdbq.core.SqlTransactionRaw;
import jdbq.mapping.MapperFactory;

import java.lang.reflect.Type;

public interface DaoContext extends MapperFactory {

    SqlParameter parameter(Type type, Object value);

    @Override
    default DaoTransaction withTransaction(SqlTransactionRaw t) {
        return new DaoTransaction(this, t);
    }
}

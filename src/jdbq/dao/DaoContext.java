package jdbq.dao;

import jdbq.core.SqlParameter;
import jdbq.core.SqlTransaction;
import jdbq.mapping.MapperContext;

import java.lang.reflect.Type;

public interface DaoContext extends MapperContext {

    SqlParameter parameter(Type type, Object value);

    @Override
    default DaoTransaction withTransaction(SqlTransaction t) {
        return new DaoTransaction(this, t);
    }
}

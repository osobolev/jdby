package jdbq.mapping;

import java.lang.reflect.Type;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface CheckColumnCompatibility {

    // todo: move to column mapper directly???
    Boolean check(ResultSetMetaData rsmd, int index, Type javaType) throws SQLException;
}

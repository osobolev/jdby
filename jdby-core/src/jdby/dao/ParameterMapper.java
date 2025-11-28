package jdby.dao;

import jdby.core.SqlParameter;

public interface ParameterMapper {

    SqlParameter toSql(Object value);
}

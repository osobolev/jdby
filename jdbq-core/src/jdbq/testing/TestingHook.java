package jdbq.testing;

import jdbq.mapping.ColumnMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TestingHook {

    void checkRowType(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException;

    void checkColumn(ResultSet rs, Type javaType, ColumnMapper columnMapper) throws SQLException;
}

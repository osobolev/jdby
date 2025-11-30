package jdby.testing;

import jdby.core.testing.SqlTestingHook;
import jdby.mapping.ColumnMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

final class TestingHookImpl implements SqlTestingHook.Checker {

    private final TestingOptions options;

    TestingHookImpl(TestingOptions options) {
        this.options = options;
    }

    private CheckCompatibility getChecker(ResultSet rs) throws SQLException {
        return new CheckCompatibility(options, rs.getMetaData());
    }

    @Override
    public void checkRowType(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException {
        getChecker(rs).checkRecord(rs, rowType, columnMappers, sqlNames);
    }

    @Override
    public void checkColumn(ResultSet rs, Type javaType, ColumnMapper columnMapper) throws SQLException {
        getChecker(rs).checkColumn(javaType, "column", 1, columnMapper);
    }
}

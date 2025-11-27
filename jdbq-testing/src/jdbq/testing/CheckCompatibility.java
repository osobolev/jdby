package jdbq.testing;

import jdbq.mapping.CheckColumnCompatibility;
import jdbq.mapping.ColumnMapper;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// todo: modes: check only column correspondence, basic typecheck, strict typecheck (int withds)
// todo: optional warning output
// todo: treat errors as warnings/warnings as errors
final class CheckCompatibility {

    private final TestingOptions options;
    private final ResultSetMetaData rsmd;

    CheckCompatibility(TestingOptions options, ResultSetMetaData rsmd) {
        this.options = options;
        this.rsmd = rsmd;
    }

    void checkColumn(Type javaType, String javaName, int index, ColumnMapper columnMapper) throws SQLException {
        JDBCType dbType = JDBCType.valueOf(rsmd.getColumnType(index));
        List<CheckColumnCompatibility> checks = Arrays.asList(options.check, columnMapper.checkCompatibility());
        new CheckOneColumn(rsmd, index, dbType, javaType, javaName, checks).checkCompatibility();
    }

    private void checkColumn(RecordComponent component, int index, ColumnMapper columnMapper) throws SQLException {
        checkColumn(
            component.getType(), component.getDeclaringRecord().getSimpleName() + "." + component.getName(),
            index, columnMapper
        );
    }

    private void checkPosition(Class<?> rowType, List<ColumnMapper> columnMappers) throws SQLException {
        RecordComponent[] components = rowType.getRecordComponents();
        int columnCount = rsmd.getColumnCount();
        if (columnCount != components.length) {
            throw new IllegalArgumentException(String.format(
                "Row type %s has %s columns (%s) than select (%s)",
                rowType.getName(), components.length > columnCount ? "more" : "less", components.length, columnCount
            ));
        }
        for (int i = 0; i < components.length; i++) {
            checkColumn(components[i], i, columnMappers.get(i));
            // todo: check that names are similar (warn)???
        }
    }

    private void checkName(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException {
        int[] indexes = new int[columnMappers.size()];
        Set<Integer> usedSqlColumns = new HashSet<>();
        for (int i = 0; i < columnMappers.size(); i++) {
            String sqlName = sqlNames.get(i);
            int index = rs.findColumn(sqlName);
            if (!usedSqlColumns.add(index)) {
                // todo: warn about duplicate???
            }
            indexes[i] = index;
        }
        int columnCount = rsmd.getColumnCount();
        if (usedSqlColumns.size() < columnCount) {
            String unused = IntStream
                .rangeClosed(1, columnCount)
                .filter(i -> !usedSqlColumns.contains(i))
                .mapToObj(i -> CheckOneColumn.dbColumnName(rsmd, i))
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(String.format("Columns %s are not used", unused));
        }
        RecordComponent[] components = rowType.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            checkColumn(components[i], indexes[i], columnMappers.get(i));
        }
    }

    void checkRecord(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException {
        if (sqlNames == null) {
            checkPosition(rowType, columnMappers);
        } else {
            checkName(rs, rowType, columnMappers, sqlNames);
        }
    }
}

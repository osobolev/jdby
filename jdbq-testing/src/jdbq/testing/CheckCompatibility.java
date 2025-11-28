package jdbq.testing;

import jdbq.mapping.CheckColumnCompatibility;
import jdbq.mapping.ColumnMapper;
import jdbq.mapping.ColumnNaming;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jdbq.testing.CheckOneColumn.dbColumnName;

final class CheckCompatibility {

    private final TestingOptions options;
    private final ResultSetMetaData rsmd;

    CheckCompatibility(TestingOptions options, ResultSetMetaData rsmd) {
        this.options = options;
        this.rsmd = rsmd;
    }

    void checkColumn(Type javaType, String javaName, int index, ColumnMapper columnMapper) throws SQLException {
        if (options.strictness == TestStrictness.NO_TYPE_CHECK)
            return;
        JDBCType dbType = JDBCType.valueOf(rsmd.getColumnType(index));
        List<CheckColumnCompatibility> checks = Arrays.asList(options.checkColumns, columnMapper.checkCompatibility());
        new CheckOneColumn(options, rsmd, index, dbType, javaType, javaName, checks).checkCompatibility();
    }

    private void checkColumn(RecordComponent component, int index, ColumnMapper columnMapper) throws SQLException {
        checkColumn(
            component.getType(), component.getDeclaringRecord().getSimpleName() + "." + component.getName(),
            index, columnMapper
        );
    }

    private static String canonicalize(String name) {
        return name.replace("_", "").toLowerCase();
    }

    private void checkNamesAreSimilar(Class<?> rowType, int index, RecordComponent component) throws SQLException {
        String dbName = rsmd.getColumnName(index);
        if (dbName == null || dbName.isEmpty())
            return;
        String javaName = ColumnNaming.sqlNameFromAnnotation(component, Function.identity());
        if (!Objects.equals(canonicalize(dbName), canonicalize(javaName))) {
            options.warn(String.format(
                "Java field '%s' and DB column '%s' have different names for row type '%s'",
                javaName, dbName, rowType.getName()
            ));
        }
    }

    private void checkPosition(Class<?> rowType, List<ColumnMapper> columnMappers) throws SQLException {
        RecordComponent[] components = rowType.getRecordComponents();
        int columnCount = rsmd.getColumnCount();
        if (columnCount != components.length) {
            options.error(String.format(
                "Row type '%s' has %s columns (%s) than select (%s)",
                rowType.getName(), components.length > columnCount ? "more" : "less", components.length, columnCount
            ));
        }
        for (int i = 0; i < Math.min(components.length, columnCount); i++) {
            int index = i + 1;
            RecordComponent component = components[i];
            checkColumn(component, index, columnMappers.get(i));
            if (options.checkNamesForPositions) {
                checkNamesAreSimilar(rowType, index, component);
            }
        }
    }

    private void checkName(ResultSet rs, Class<?> rowType, List<ColumnMapper> columnMappers, List<String> sqlNames) throws SQLException {
        int[] indexes = new int[columnMappers.size()];
        Set<Integer> usedSqlColumns = new HashSet<>();
        for (int i = 0; i < columnMappers.size(); i++) {
            String sqlName = sqlNames.get(i);
            int index = rs.findColumn(sqlName);
            if (!usedSqlColumns.add(index)) {
                options.warn(String.format(
                    "Column %s is used more than once when mapping row type %s",
                    dbColumnName(rsmd, index), rowType.getName()
                ));
            }
            indexes[i] = index;
        }
        int columnCount = rsmd.getColumnCount();
        if (usedSqlColumns.size() < columnCount) {
            String unused = IntStream
                .rangeClosed(1, columnCount)
                .filter(i -> !usedSqlColumns.contains(i))
                .mapToObj(i -> dbColumnName(rsmd, i))
                .collect(Collectors.joining(", "));
            options.error(String.format(
                "Columns %s are not used when mapping to row type %s",
                unused, rowType.getName()
            ));
        }
        RecordComponent[] components = rowType.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            checkColumn(component, indexes[i], columnMappers.get(i));
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

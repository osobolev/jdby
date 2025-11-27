package jdbq.mapping;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class CheckCompatibility {

    private final ResultSetMetaData rsmd;
    private final CheckColumnCompatibility check = DefaultMapperFactory.check;

    CheckCompatibility(ResultSetMetaData rsmd) {
        this.rsmd = rsmd;
    }

    private static int intLen(Class<?> type) {
        if (type == byte.class || type == Byte.class)
            return 1;
        if (type == short.class || type == Short.class)
            return 2;
        if (type == int.class || type == Integer.class)
            return 4;
        if (type == long.class || type == Long.class)
            return 8;
        if (type == BigInteger.class)
            return Integer.MAX_VALUE;
        return 0;
    }

    private static boolean fitsIntoPrecision(Class<?> cls, int precision) {
        // todo: customize this check
        int intLen = intLen(cls);
        return switch (intLen) {
            case 0 -> false;
            case 1 -> precision <= 2; // byte can fit only 2 digits
            case 2 -> precision <= 4; // short can fit only 4 digits
            case 4 -> precision <= 9; // int can fit only 9 digits
            case 8 -> precision <= 18; // long can fit only 18 digits
            default -> true;
        };
    }

    private boolean isNumericCompatible(int index, JDBCType dbType, Class<?> type) throws SQLException {
        if (type == double.class || type == Double.class ||
            type == float.class || type == Float.class ||
            type == BigDecimal.class) {
            return dbType == JDBCType.DOUBLE || dbType == JDBCType.FLOAT || dbType == JDBCType.REAL ||
                   dbType == JDBCType.NUMERIC || dbType == JDBCType.DECIMAL ||
                   dbType == JDBCType.TINYINT || dbType == JDBCType.SMALLINT ||
                   dbType == JDBCType.INTEGER || dbType == JDBCType.BIGINT;
        }
        return switch (dbType) {
            case TINYINT -> intLen(type) >= 1;
            case SMALLINT -> intLen(type) >= 2;
            case INTEGER -> intLen(type) >= 4;
            case BIGINT -> intLen(type) >= 8;
            case NUMERIC, DECIMAL -> {
                int precision = rsmd.getPrecision(index);
                int scale = rsmd.getScale(index);
                if (scale > 0)
                    yield false;
                yield fitsIntoPrecision(type, precision - scale);
            }
            default -> false;
        };
    }

    private boolean isCompatible(int index, JDBCType dbType, Class<?> type) throws SQLException {
        return switch (dbType) {
        case VARCHAR, NVARCHAR,
             CHAR, NCHAR,
             LONGVARCHAR, LONGNVARCHAR,
             CLOB, NCLOB -> type == String.class;
        case BINARY, VARBINARY, LONGVARBINARY, BLOB -> type == byte[].class;
        case BIT, BOOLEAN -> type == boolean.class || type == Boolean.class;
        case DATE -> type == LocalDate.class;
        case TIME -> type == LocalTime.class;
        case TIMESTAMP_WITH_TIMEZONE -> type == OffsetDateTime.class;
        case TIMESTAMP -> type == LocalDateTime.class;
        default -> isNumericCompatible(index, dbType, type);
        };
    }

    private String dbColumnName(int index) {
        String add = "";
        try {
            String columnName = rsmd.getColumnName(index);
            String tableName = rsmd.getTableName(index);
            if (tableName != null && !tableName.isEmpty() && columnName != null && !columnName.isEmpty()) {
                add = " (" + tableName + "." + columnName + ")";
            } else if (columnName != null && !columnName.isEmpty()) {
                add = " (" + columnName + ")";
            }
        } catch (SQLException ex) {
            // ignore
        }
        return index + add;
    }

    private String sqlTypeName(int index, JDBCType dbType) {
        try {
            switch (dbType) {
            case CHAR, VARCHAR, LONGVARCHAR,
                 NCHAR, NVARCHAR, LONGNVARCHAR,
                 BINARY, VARBINARY, LONGVARBINARY -> {
                int size = rsmd.getColumnDisplaySize(index);
                if (size > 0) {
                    return dbType + "(" + size + ")";
                }
            }
            case NUMERIC, DECIMAL -> {
                int precision = rsmd.getPrecision(index);
                if (precision > 0) {
                    int scale = rsmd.getScale(index);
                    if (scale == 0) {
                        return dbType + "(" + precision + ")";
                    } else {
                        return dbType + "(" + precision + ", " + scale + ")";
                    }
                }
            }
            }
        } catch (SQLException ex) {
            // ignore
        }
        return dbType.toString();
    }

    private Boolean check(CheckColumnCompatibility check, int index, Type javaType) throws SQLException {
        return check == null ? null : check.check(rsmd, index, javaType);
    }

    private boolean isCompatible(Type javaType, int index, JDBCType dbType,
                                 CheckColumnCompatibility fromMapper) throws SQLException {
        {
            Boolean ok = check(fromMapper, index, javaType);
            if (ok != null) {
                return ok.booleanValue();
            }
        }
        {
            Boolean ok = check(check, index, javaType);
            if (ok != null) {
                return ok.booleanValue();
            }
        }
        // No custom check, compare java type with DB type
        if (javaType instanceof Class<?> cls) {
            return isCompatible(index, dbType, cls);
        } else {
            return false;
        }
    }

    void checkCompatibility(Type javaType, String javaName, int index,
                            CheckColumnCompatibility fromMapper) throws SQLException {
        JDBCType dbType = JDBCType.valueOf(rsmd.getColumnType(index));
        if (!isCompatible(javaType, index, dbType, fromMapper)) {
            throw new IllegalArgumentException(String.format(
                "Column %s of type %s is not compatible with field (%s) of type %s",
                dbColumnName(index), sqlTypeName(index, dbType),
                javaName, javaType
            ));
        }
    }

    private void checkCompatibility(RecordComponent component, int index,
                                    CheckColumnCompatibility fromMapper) throws SQLException {
        checkCompatibility(
            component.getType(), component.getDeclaringRecord().getSimpleName() + "." + component.getName(),
            index, fromMapper
        );
    }

    void checkPosition(Class<?> rowType, List<ColumnMapper> columnMappers) throws SQLException {
        RecordComponent[] components = rowType.getRecordComponents();
        int columnCount = rsmd.getColumnCount();
        if (columnCount != components.length) {
            throw new IllegalArgumentException(String.format(
                "Row type %s has %s columns (%s) than select (%s)",
                rowType.getName(), components.length > columnCount ? "more" : "less", components.length, columnCount
            ));
        }
        for (int i = 0; i < columnCount; i++) {
            ColumnMapper columnMapper = columnMappers.get(i);
            checkCompatibility(components[i], i, columnMapper.checkCompatibility());
            // todo: check that names are similar (warn)???
        }
    }

    void checkName(ResultSet rs, Class<?> rowType, List<NamedColumn> columnNames) throws SQLException {
        int[] indexes = new int[columnNames.size()];
        Set<Integer> usedSqlColumns = new HashSet<>();
        for (int i = 0; i < columnNames.size(); i++) {
            String sqlName = columnNames.get(i).sqlName();
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
                .mapToObj(this::dbColumnName)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(String.format("Columns %s are not used", unused));
        }
        RecordComponent[] components = rowType.getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            ColumnMapper columnMapper = columnNames.get(i).mapper();
            checkCompatibility(components[i], indexes[i], columnMapper.checkCompatibility());
        }
    }
}

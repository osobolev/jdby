package jdbq.testing;

import jdbq.mapping.CheckColumnCompatibility;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

final class CheckOneColumn {

    private final TestingOptions options;
    private final ResultSetMetaData rsmd;
    private final int index;
    private final JDBCType dbType;
    private final Type javaType;
    private final String javaName;
    private final List<CheckColumnCompatibility> checks;

    CheckOneColumn(TestingOptions options, ResultSetMetaData rsmd, int index, JDBCType dbType, Type javaType, String javaName, List<CheckColumnCompatibility> checks) {
        this.options = options;
        this.rsmd = rsmd;
        this.index = index;
        this.dbType = dbType;
        this.javaType = javaType;
        this.javaName = javaName;
        this.checks = checks;
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

    private boolean isNumericCompatible(Class<?> type) throws SQLException {
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

    private boolean isCompatible(Class<?> type) throws SQLException {
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
        default -> isNumericCompatible(type);
        };
    }

    static String dbColumnName(ResultSetMetaData rsmd, int index) {
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

    private String sqlTypeName() {
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

    private boolean isCompatible() throws SQLException {
        for (CheckColumnCompatibility check : checks) {
            if (check == null)
                continue;
            Boolean ok = check.check(rsmd, index, javaType);
            if (ok != null) {
                return ok.booleanValue();
            }
        }
        // No custom check, compare java type with DB type
        if (javaType instanceof Class<?> cls) {
            return isCompatible(cls);
        } else {
            return false;
        }
    }

    void checkCompatibility() throws SQLException {
        if (!isCompatible()) {
            throw new IllegalArgumentException(String.format(
                "Column %s of type %s is not compatible with field (%s) of type %s",
                dbColumnName(rsmd, index), sqlTypeName(),
                javaName, javaType
            ));
        }
    }
}

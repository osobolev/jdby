package jdby.core;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface SqlParameter {

    void set(PreparedStatement ps, int index) throws SQLException;

    static SqlParameter pInt(Integer x) {
        return new SimpleSqlParameter(x, JDBCType.INTEGER);
    }

    static SqlParameter pLong(Long x) {
        return new SimpleSqlParameter(x, JDBCType.BIGINT);
    }

    static SqlParameter pDouble(Double x) {
        return new SimpleSqlParameter(x, JDBCType.DOUBLE);
    }

    static SqlParameter pBoolean(Boolean x) {
        return new SimpleSqlParameter(x, JDBCType.BOOLEAN);
    }

    static SqlParameter pString(String x) {
        return new SimpleSqlParameter(x, JDBCType.VARCHAR);
    }

    static SqlParameter pDate(LocalDate x) {
        return new SimpleSqlParameter(x, JDBCType.DATE);
    }

    static SqlParameter pDateTime(LocalDateTime x) {
        return new SimpleSqlParameter(x, JDBCType.TIMESTAMP);
    }

    static SqlParameter pDateTime(OffsetDateTime x) {
        return new SimpleSqlParameter(x, JDBCType.TIMESTAMP_WITH_TIMEZONE);
    }

    static SqlParameter jdbc(Object x, JDBCType type) {
        return new SimpleSqlParameter(x, type);
    }
}

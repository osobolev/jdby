package jdby.core;

import jdby.core.testing.SqlTestingHook;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Query implements QueryLike {

    private final String sql;
    private final List<SqlParameter> parameters;

    public Query(String sql, List<SqlParameter> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public Query(String sql, SqlParameter... parameters) {
        this(sql, Arrays.asList(parameters));
    }

    public static Query sql(String sql, SqlParameter... parameters) {
        return new Query(sql, parameters);
    }

    public static QueryBuilder builder(String... sql) {
        QueryBuilder buf = new QueryBuilder();
        for (String s : sql) {
            buf.append(s);
        }
        return buf;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public List<SqlParameter> getParameters() {
        return parameters;
    }

    @Override
    public Query toQuery() {
        return this;
    }

    @Override
    public String toString() {
        return sql;
    }

    public void setParameters(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            SqlParameter parameter = parameters.get(i);
            parameter.set(ps, i + 1);
        }
    }

    private PreparedStatement preparedStatement(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setParameters(ps);
        return ps;
    }

    public <T> List<T> listRows(Connection connection, RowMapper<T> rowMapper) {
        try (PreparedStatement ps = preparedStatement(connection)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTestingHook.isTesting()) {
                    rowMapper.mapRow(rs);
                    return Collections.emptyList();
                }
                return rowMapper.mapAllRows(rs);
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public <T> List<T> listRows(RowConnection connection, Class<T> rowType) {
        return listRows(connection.getConnection(), connection.rowMapper(rowType));
    }

    private <T> T oneRow(Connection connection, boolean canHaveNone, boolean canHaveMore, RowMapper<T> rowMapper) {
        try (PreparedStatement ps = preparedStatement(connection)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTestingHook.isTesting()) {
                    return rowMapper.mapRow(rs);
                }
                if (rs.next()) {
                    T row = rowMapper.mapRow(rs);
                    if (!canHaveMore && rs.next()) {
                        throw new UncheckedSQLException("21000", "More than one rows found");
                    }
                    return row;
                } else {
                    if (!canHaveNone) {
                        throw new UncheckedSQLException("02000", "No rows found");
                    }
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public <T> T exactlyOneRow(Connection connection, RowMapper<T> rowMapper) {
        return oneRow(connection, false, false, rowMapper);
    }

    public <T> T exactlyOneRow(RowConnection connection, Class<T> rowType) {
        return exactlyOneRow(connection.getConnection(), connection.rowMapper(rowType));
    }

    public <T> T maybeRow(Connection connection, RowMapper<T> rowMapper) {
        return oneRow(connection, true, false, rowMapper);
    }

    public <T> T maybeRow(RowConnection connection, Class<T> rowType) {
        return maybeRow(connection.getConnection(), connection.rowMapper(rowType));
    }

    public <T> T firstRow(Connection connection, RowMapper<T> rowMapper) {
        return oneRow(connection, true, true, rowMapper);
    }

    public <T> T firstRow(RowConnection connection, Class<T> rowType) {
        return firstRow(connection.getConnection(), connection.rowMapper(rowType));
    }

    private static int executeUpdate(PreparedStatement ps) throws SQLException {
        if (SqlTestingHook.isTesting()) {
            try {
                ps.executeUpdate();
            } catch (SQLException ex) {
                if (!(ex instanceof SQLIntegrityConstraintViolationException)) {
                    String sqlState = ex.getSQLState();
                    if (sqlState == null || !sqlState.startsWith("23"))
                        throw ex;
                }
            }
            return 1;
        }
        return ps.executeUpdate();
    }

    public int executeUpdate(Connection connection) {
        try (PreparedStatement ps = preparedStatement(connection)) {
            return executeUpdate(ps);
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public int executeUpdate(RowConnection connection) {
        return executeUpdate(connection.getConnection());
    }

    public <T> T executeUpdate(Connection connection, GeneratedKeyMapper<T> keyMapper,
                               String generatedColumn, String... otherGeneratedColumns) {
        String[] generatedColumns = new String[1 + otherGeneratedColumns.length];
        generatedColumns[0] = generatedColumn;
        System.arraycopy(otherGeneratedColumns, 0, generatedColumns, 1, otherGeneratedColumns.length);
        try (PreparedStatement ps = connection.prepareStatement(sql, generatedColumns)) {
            setParameters(ps);
            int rows = executeUpdate(ps);
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return keyMapper.map(rows, generatedColumns, rs);
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public <T> T insertRow(RowConnection connection, String generatedColumn, Class<T> keyType) {
        return executeUpdate(connection.getConnection(), connection.keyMapper(keyType), generatedColumn);
    }
}

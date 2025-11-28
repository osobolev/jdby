package jdbq.core;

import jdbq.core.testing.SqlTestingHook;

import java.sql.*;
import java.util.ArrayList;
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

    public <T> List<T> listRows(Connection connection, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(connection)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTestingHook.isTesting()) {
                    rowMapper.mapRow(rs);
                    return Collections.emptyList();
                }
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    T row = rowMapper.mapRow(rs);
                    list.add(row);
                }
                return list;
            }
        }
    }

    public <T> List<T> listRows(RowConnection connection, Class<T> rowType) throws SQLException {
        return listRows(connection.getConnection(), connection.rowMapper(rowType));
    }

    public <T> T exactlyOneRow(Connection connection, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(connection)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTestingHook.isTesting()) {
                    return rowMapper.mapRow(rs);
                }
                if (rs.next()) {
                    T row = rowMapper.mapRow(rs);
                    if (rs.next())
                        throw new SQLException("More than one rows found");
                    return row;
                } else {
                    throw new SQLException("No rows found");
                }
            }
        }
    }

    public <T> T exactlyOneRow(RowConnection connection, Class<T> rowType) throws SQLException {
        return exactlyOneRow(connection.getConnection(), connection.rowMapper(rowType));
    }

    public <T> T maybeRow(Connection connection, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(connection)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTestingHook.isTesting()) {
                    return rowMapper.mapRow(rs);
                }
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public <T> T maybeRow(RowConnection connection, Class<T> rowType) throws SQLException {
        return maybeRow(connection.getConnection(), connection.rowMapper(rowType));
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

    public int executeUpdate(Connection connection) throws SQLException {
        try (PreparedStatement ps = preparedStatement(connection)) {
            return executeUpdate(ps);
        }
    }

    public int executeUpdate(RowConnection connection) throws SQLException {
        return executeUpdate(connection.getConnection());
    }

    public <T> T executeUpdate(Connection connection, GeneratedKeyMapper<T> keyMapper,
                               String generatedColumn, String... otherGeneratedColumns) throws SQLException {
        String[] generatedColumns = new String[1 + otherGeneratedColumns.length];
        generatedColumns[0] = generatedColumn;
        System.arraycopy(otherGeneratedColumns, 0, generatedColumns, 1, otherGeneratedColumns.length);
        try (PreparedStatement ps = connection.prepareStatement(sql, generatedColumns)) {
            setParameters(ps);
            int rows = executeUpdate(ps);
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return keyMapper.map(rows, generatedColumns, rs);
            }
        }
    }

    public <T> T executeUpdate(RowConnection connection, Class<T> keyType,
                               String generatedColumn, String... otherGeneratedColumns) throws SQLException {
        return executeUpdate(
            connection.getConnection(), connection.keyMapper(keyType),
            generatedColumn, otherGeneratedColumns
        );
    }
}

package jdbq.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Query jdbcSql(String sql, SqlParameter... parameters) {
        return new Query(sql, parameters);
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

    private PreparedStatement preparedStatement(SqlTransaction t) throws SQLException {
        PreparedStatement ps = t.getConnection().prepareStatement(sql);
        setParameters(ps);
        return ps;
    }

    public <T> List<T> listRows(SqlTransaction t, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(t)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTesting.testing) {
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

    public <T> List<T> listRows(RowTransaction t, Class<T> rowType) throws SQLException {
        return listRows(t, t.rowMapper(rowType));
    }

    public <T> T exactlyOneRow(SqlTransaction t, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(t)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTesting.testing) {
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

    public <T> T exactlyOneRow(RowTransaction t, Class<T> rowType) throws SQLException {
        return exactlyOneRow(t, t.rowMapper(rowType));
    }

    public <T> T maybeRow(SqlTransaction t, RowMapper<T> rowMapper) throws SQLException {
        try (PreparedStatement ps = preparedStatement(t)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (SqlTesting.testing) {
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

    public <T> T maybeRow(RowTransaction t, Class<T> rowType) throws SQLException {
        return maybeRow(t, t.rowMapper(rowType));
    }

    public int executeUpdate(SqlTransaction t) throws SQLException {
        try (PreparedStatement ps = preparedStatement(t)) {
            return ps.executeUpdate();
        }
    }

    public <T> T executeUpdate(SqlTransaction t, GeneratedKeyMapper<T> keyMapper, String... generatedColumns) throws SQLException {
        try (PreparedStatement ps = preparedStatement(t)) {
            int rows = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return keyMapper.map(rows, generatedColumns, rs);
            }
        }
    }

    public <T> T executeUpdate(RowTransaction t, Class<T> keyType, String... generatedColumns) throws SQLException {
        return executeUpdate(t, t.keyMapper(keyType), generatedColumns);
    }
}

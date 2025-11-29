package jdby.dao;

import jdby.core.*;

import java.sql.SQLException;
import java.util.List;

import static jdby.dao.DaoProxies.getCallData;

public final class SqlCommands {

    public static void parameter(String name, SqlParameter value) {
        CallData data = getCallData();
        data.parameters.put(name, value);
    }

    public static <T> void parameter(String name, T value, Class<T> cls) {
        CallData data = getCallData();
        data.parameters.put(name, data.ctx.parameter(cls, value));
    }

    public static RowConnection getConnection() {
        CallData data = getCallData();
        return data.ctx.withConnection(data.connection);
    }

    public static SqlBuilder builder(String... sql) {
        SqlBuilder buf = new SqlBuilder();
        for (String s : sql) {
            buf.append(s);
        }
        return buf;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listRows(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (List<T>) query.listRows(data.connection, data.rowMapper(List.class));
    }

    @SuppressWarnings("unchecked")
    public static <T> T exactlyOneRow(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.exactlyOneRow(data.connection, data.rowMapper(null));
    }

    @SuppressWarnings("unchecked")
    public static <T> T maybeRow(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.maybeRow(data.connection, data.rowMapper(null));
    }

    public static int executeUpdate(CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return query.executeUpdate(data.connection);
    }

    public record ColumnName(String name) {}

    public static ColumnName column(String name) {
        return new ColumnName(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> T insertRow(ColumnName generatedColumn, CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return (T) query.executeUpdate(data.connection, data.keyMapper(), generatedColumn.name());
    }

    public static <T> T executeUpdate(CharSequence sql, GeneratedKeyMapper<T> keyMapper, String generatedColumn, String... otherGeneratedColumns) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return query.executeUpdate(data.connection, keyMapper, generatedColumn, otherGeneratedColumns);
    }

    public static void executeBatch(Batch batch, CharSequence sql) throws SQLException {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        batch.addBatch(data.connection, query);
    }
}

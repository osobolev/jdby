package jdby.dao;

import jdby.core.*;

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
        return data.ctx.getMapperContext().withConnection(data.connection);
    }

    public static SqlBuilder builder(String... sql) {
        SqlBuilder buf = new SqlBuilder();
        for (String s : sql) {
            buf.append(s);
        }
        return buf;
    }

    public static <T> List<T> listRows(CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return data.cast(query.listRows(data.connection, data.rowMapper(true)));
    }

    public static <T> T exactlyOneRow(CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return data.cast(query.exactlyOneRow(data.connection, data.rowMapper(false)));
    }

    public static <T> T maybeRow(CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return data.cast(query.maybeRow(data.connection, data.rowMapper(false)));
    }

    public static int executeUpdate(CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return query.executeUpdate(data.connection);
    }

    public record ColumnName(String name) {}

    public static ColumnName column(String name) {
        return new ColumnName(name);
    }

    public static <T> T insertRow(ColumnName generatedColumn, CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return data.cast(query.executeUpdate(data.connection, data.keyMapper(), generatedColumn.name()));
    }

    public static <T> T executeUpdate(CharSequence sql, GeneratedKeyMapper<T> keyMapper, String generatedColumn, String... otherGeneratedColumns) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        return query.executeUpdate(data.connection, keyMapper, generatedColumn, otherGeneratedColumns);
    }

    public static void executeBatch(Batch batch, CharSequence sql) {
        CallData data = getCallData();
        Query query = data.substituteArgs(sql);
        batch.addBatch(data.connection, query);
    }
}

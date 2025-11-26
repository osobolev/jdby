package jdbq.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class QueryBuilder implements QueryLike {

    private final StringBuilder buf = new StringBuilder();
    private final List<SqlParameter> parameters = new ArrayList<>();

    public QueryBuilder() {
    }

    public QueryBuilder(String sql, SqlParameter... parameters) {
        buf.append(sql);
        this.parameters.addAll(Arrays.asList(parameters));
    }

    @Override
    public CharSequence getSql() {
        return buf;
    }

    @Override
    public List<SqlParameter> getParameters() {
        return parameters;
    }

    private static boolean isSpace(char ch) {
        return ch <= ' ';
    }

    public void append(CharSequence sql, List<? extends SqlParameter> parameters) {
        if (!buf.isEmpty() && !sql.isEmpty()) {
            char lastChar = buf.charAt(buf.length() - 1);
            char firstChar = sql.charAt(0);
            if (!isSpace(lastChar) && !isSpace(firstChar)) {
                buf.append(' ');
            }
        }
        buf.append(sql);
        this.parameters.addAll(parameters);
    }

    public void append(CharSequence sql, SqlParameter... parameters) {
        append(sql, Arrays.asList(parameters));
    }

    public void append(QueryLike other) {
        append(other.getSql(), other.getParameters());
    }

    public Query toQuery() {
        return new Query(buf.toString(), parameters);
    }
}

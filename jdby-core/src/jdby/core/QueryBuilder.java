package jdby.core;

import jdby.internal.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class QueryBuilder implements QueryLike {

    private final StringBuilder buf = new StringBuilder();
    private final List<SqlParameter> parameters = new ArrayList<>();

    public QueryBuilder() {
    }

    public QueryBuilder(CharSequence sql, SqlParameter... parameters) {
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

    public void append(CharSequence sql, List<? extends SqlParameter> parameters) {
        Utils.append(buf, sql);
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

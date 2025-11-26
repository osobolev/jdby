package jdbq.dao;

import jdbq.core.Query;
import jdbq.core.SqlParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ParsedQuery {

    public final String sql;
    public final List<String> paramNames;

    public ParsedQuery(String sql, List<String> paramNames) {
        this.sql = sql;
        this.paramNames = paramNames;
    }

    @Override
    public String toString() {
        return sql;
    }

    private static final class ParserState implements SqlParser.Callback {

        private final String sql;
        private final StringBuilder buf = new StringBuilder();
        private final List<String> paramNames = new ArrayList<>();
        private int start = 0;

        private ParserState(String sql) {
            this.sql = sql;
        }

        @Override
        public void ident(int from, int to, String ident) {
            buf.append(sql, start, from);
            buf.append("?");
            paramNames.add(ident);
            start = to;
        }

        void finish() {
            buf.append(sql, start, sql.length());
        }
    }

    public static ParsedQuery parse(String sql) {
        ParserState state = new ParserState(sql);
        SqlParser parser = new SqlParser(sql, state);
        parser.parse();
        state.finish();
        return new ParsedQuery(state.buf.toString(), state.paramNames);
    }

    public Query toQuery(Function<String, SqlParameter> getParameters) {
        List<SqlParameter> parameters = new ArrayList<>(paramNames.size());
        for (String paramName : paramNames) {
            SqlParameter parameter = getParameters.apply(paramName);
            if (parameter == null) {
                // todo: specify sql text???
                throw new IllegalArgumentException("Parameter " + paramName + " not found");
            }
            parameters.add(parameter);
        }
        return new Query(sql, parameters);
    }
}

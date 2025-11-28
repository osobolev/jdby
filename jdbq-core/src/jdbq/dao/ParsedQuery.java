package jdbq.dao;

import jdbq.core.Query;
import jdbq.core.SqlParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class ParsedQuery {

    final String sql;
    final List<String> paramNames;

    ParsedQuery(String sql, List<String> paramNames) {
        this.sql = sql;
        this.paramNames = paramNames;
    }

    @Override
    public String toString() {
        return sql;
    }

    private static final class ParserState implements SqlParser.Callback {

        private final CharSequence sql;
        private final StringBuilder buf;
        private final List<String> paramNames = new ArrayList<>();
        private int start = 0;

        private ParserState(CharSequence sql) {
            this.sql = sql;
            this.buf = new StringBuilder(sql.length());
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

    static ParsedQuery parse(CharSequence sql) {
        ParserState state = new ParserState(sql);
        SqlParser parser = new SqlParser(sql, state);
        parser.parse();
        state.finish();
        return new ParsedQuery(state.buf.toString(), state.paramNames);
    }

    Query toQuery(Map<String, SqlParameter> byName) {
        List<SqlParameter> parameters = new ArrayList<>(paramNames.size());
        for (String paramName : paramNames) {
            SqlParameter parameter = byName.get(paramName);
            if (parameter == null) {
                String details;
                if (byName.size() <= 10) {
                    String available;
                    if (byName.isEmpty()) {
                        available = "none";
                    } else {
                        available = byName
                            .keySet()
                            .stream()
                            .sorted()
                            .map(p -> "'" + p + "'")
                            .collect(Collectors.joining(", "));
                    }
                    details = "; available parameters: " + available;
                } else {
                    details = "";
                }
                throw new IllegalArgumentException("Value for parameter '" + paramName + "' not found" + details);
            }
            parameters.add(parameter);
        }
        return new Query(sql, parameters);
    }
}

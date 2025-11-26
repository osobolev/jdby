package jdbq.dao;

final class SqlParser {

    private final String text;
    private final Callback callback;
    private int i = 0;

    interface Callback {

        void ident(int from, int to, String ident);
    }

    SqlParser(String text, Callback callback) {
        this.text = text;
        this.callback = callback;
    }

    private boolean eof() {
        return i >= text.length();
    }

    private char ch() {
        return text.charAt(i);
    }

    private String parseIdent() {
        char ch = ch();
        if (!Character.isJavaIdentifierStart(ch))
            return null;
        i++;
        StringBuilder buf = new StringBuilder();
        buf.append(ch);
        while (!eof()) {
            char ch2 = ch();
            if (!Character.isJavaIdentifierPart(ch2))
                break;
            i++;
            buf.append(ch2);
        }
        return buf.toString();
    }

    private void skipString(char quote) {
        while (!eof()) {
            char ch = ch();
            i++;
            if (ch == quote) {
                if (eof() || ch() != quote) {
                    break;
                }
                i++;
            }
        }
    }

    void parse() {
        while (!eof()) {
            char ch = ch();
            int from = i++;
            if (ch == '\'' || ch == '"') {
                skipString(ch);
            } else if (ch == ':') {
                if (!eof()) {
                    if (ch() == ':') {
                        i++;
                    } else {
                        String ident = parseIdent();
                        if (ident != null) {
                            callback.ident(from, i, ident);
                        }
                    }
                }
            }
        }
    }
}

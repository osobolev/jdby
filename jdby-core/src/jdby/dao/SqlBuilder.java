package jdby.dao;

import jdby.internal.Utils;

import java.util.stream.IntStream;

public final class SqlBuilder implements CharSequence {

    private final StringBuilder buf = new StringBuilder();

    public SqlBuilder() {
    }

    public SqlBuilder(CharSequence sql) {
        buf.append(sql);
    }

    public void append(CharSequence sql) {
        Utils.append(buf, sql);
    }

    @Override
    public int length() {
        return buf.length();
    }

    @Override
    public char charAt(int index) {
        return buf.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return buf.subSequence(start, end);
    }

    @Override
    public boolean isEmpty() {
        return buf.isEmpty();
    }

    @Override
    public IntStream chars() {
        return buf.chars();
    }

    @Override
    public IntStream codePoints() {
        return buf.codePoints();
    }
}

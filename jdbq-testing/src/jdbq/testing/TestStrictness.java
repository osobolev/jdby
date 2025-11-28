package jdbq.testing;

public enum TestStrictness {
    /**
     * Only column names and column count are checked
     */
    NO_TYPE_CHECK,
    /**
     * Java field types are checked against DB column types, but {@code int} fields
     * are allowed for {@code NUMBER(20)} columns
     */
    RELAXED_TYPE_CHECK,
    /**
     * Integral field types are strictly checked againt DB column sizes:
     * <ul>
     *     <li>byte: maximum NUMERIC(2)</li>
     *     <li>short: maximum NUMERIC(4)</li>
     *     <li>int: maximum NUMERIC(9)</li>
     *     <li>long: maximum NUMERIC(18)</li>
     * </ul>
     */
    STRICT_TYPE_CHECK
}

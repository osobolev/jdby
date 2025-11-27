package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleColumnMapper<T> implements ColumnMapper {

    private interface ByIndex<T> {

        T get(ResultSet rs, int index) throws SQLException;
    }

    private interface ByName<T> {

        T get(ResultSet rs, String name) throws SQLException;
    }

    final Class<T> cls;
    private final ByIndex<T> byIndex;
    private final ByName<T> byName;

    private SimpleColumnMapper(Class<T> cls, ByIndex<T> byIndex, ByName<T> byName) {
        this.cls = cls;
        this.byIndex = byIndex;
        this.byName = byName;
    }

    public static SimpleColumnMapper<Byte> byteMapper() {
        return new SimpleColumnMapper<>(byte.class, ResultSet::getByte, ResultSet::getByte);
    }

    public static SimpleColumnMapper<Short> shortMapper() {
        return new SimpleColumnMapper<>(short.class, ResultSet::getShort, ResultSet::getShort);
    }

    public static SimpleColumnMapper<Integer> intMapper() {
        return new SimpleColumnMapper<>(int.class, ResultSet::getInt, ResultSet::getInt);
    }

    public static SimpleColumnMapper<Long> longMapper() {
        return new SimpleColumnMapper<>(long.class, ResultSet::getLong, ResultSet::getLong);
    }

    public static SimpleColumnMapper<Float> floatMapper() {
        return new SimpleColumnMapper<>(float.class, ResultSet::getFloat, ResultSet::getFloat);
    }

    public static SimpleColumnMapper<Double> doubleMapper() {
        return new SimpleColumnMapper<>(double.class, ResultSet::getDouble, ResultSet::getDouble);
    }

    public static SimpleColumnMapper<Boolean> booleanMapper() {
        return new SimpleColumnMapper<>(boolean.class, ResultSet::getBoolean, ResultSet::getBoolean);
    }

    public static <T> SimpleColumnMapper<T> jdbcMapper(Class<T> cls) {
        return new SimpleColumnMapper<>(
            cls,
            (rs, index) -> rs.getObject(index, cls),
            (rs, name) -> rs.getObject(name, cls)
        );
    }

    @Override
    public T getColumn(ResultSet rs, int index) throws SQLException {
        return byIndex.get(rs, index);
    }

    @Override
    public T getColumn(ResultSet rs, String name) throws SQLException {
        return byName.get(rs, name);
    }
}

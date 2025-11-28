package jdbq.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ColumnMapper {

    Object getColumn(ResultSet rs, int index) throws SQLException;

    Object getColumn(ResultSet rs, String name) throws SQLException;

    default CheckColumnCompatibility checkCompatibility() {
        return null;
    }

    static SimpleColumnMapper<Byte> byteMapper() {
        return new SimpleColumnMapper<>(ResultSet::getByte, ResultSet::getByte);
    }

    static SimpleColumnMapper<Short> shortMapper() {
        return new SimpleColumnMapper<>(ResultSet::getShort, ResultSet::getShort);
    }

    static SimpleColumnMapper<Integer> intMapper() {
        return new SimpleColumnMapper<>(ResultSet::getInt, ResultSet::getInt);
    }

    static SimpleColumnMapper<Long> longMapper() {
        return new SimpleColumnMapper<>(ResultSet::getLong, ResultSet::getLong);
    }

    static SimpleColumnMapper<Float> floatMapper() {
        return new SimpleColumnMapper<>(ResultSet::getFloat, ResultSet::getFloat);
    }

    static SimpleColumnMapper<Double> doubleMapper() {
        return new SimpleColumnMapper<>(ResultSet::getDouble, ResultSet::getDouble);
    }

    static SimpleColumnMapper<Boolean> booleanMapper() {
        return new SimpleColumnMapper<>(ResultSet::getBoolean, ResultSet::getBoolean);
    }

    static <T> SimpleColumnMapper<T> jdbcMapper(Class<T> cls) {
        return new SimpleColumnMapper<>(
            (rs, index) -> rs.getObject(index, cls),
            (rs, name) -> rs.getObject(name, cls)
        );
    }
}

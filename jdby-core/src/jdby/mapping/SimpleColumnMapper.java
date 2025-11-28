package jdby.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

final class SimpleColumnMapper<T> implements ColumnMapper {

    interface ByIndex<T> {

        T get(ResultSet rs, int index) throws SQLException;
    }

    interface ByName<T> {

        T get(ResultSet rs, String name) throws SQLException;
    }

    private final ByIndex<T> byIndex;
    private final ByName<T> byName;

    SimpleColumnMapper(ByIndex<T> byIndex, ByName<T> byName) {
        this.byIndex = byIndex;
        this.byName = byName;
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

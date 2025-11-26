package jdbq.mapping;

import jdbq.core.RowMapperFactory;

import java.lang.reflect.Type;

public interface MapperFactory extends RowMapperFactory {

    ColumnMapperPosition positionColumnMapper(Type type);

    ColumnMapperName nameColumnMapper(Type type);
}

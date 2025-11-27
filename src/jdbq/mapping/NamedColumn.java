package jdbq.mapping;

record NamedColumn(
    String sqlName,
    ColumnMapperName mapper
) {}

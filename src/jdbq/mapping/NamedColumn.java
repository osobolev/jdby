package jdbq.mapping;

record NamedColumn(
    String sqlName,
    ColumnMapper mapper
) {}

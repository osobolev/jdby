module io.github.osobolev.jdby.core {
    exports jdby.core;
    exports jdby.core.testing;
    exports jdby.dao;
    exports jdby.mapping;

    requires transitive java.sql;
}

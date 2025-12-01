module io.github.osobolev.jdby.core {
    exports jdby.core;
    exports jdby.core.testing;
    exports jdby.dao;
    exports jdby.mapping;
    exports jdby.transaction;
    exports jdby.internal to io.github.osobolev.jdby.testing;

    requires transitive java.sql;
}

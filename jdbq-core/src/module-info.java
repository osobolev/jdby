module io.github.osobolev.jdbq.core {
    exports jdbq.core;
    exports jdbq.core.testing;
    exports jdbq.dao;
    exports jdbq.mapping;

    requires transitive java.sql;
}

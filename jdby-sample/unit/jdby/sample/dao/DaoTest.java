package jdby.sample.dao;

import jdby.dao.DefaultDaoContext;
import jdby.mapping.ColumnNaming;
import jdby.testing.SqlTesting;
import jdby.testing.TestingOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DaoTest {

    @Test
    public void testDao() throws Exception {
        TestingOptions options = new TestingOptions();
        DefaultDaoContext ctx = new DefaultDaoContext(ColumnNaming.camelCase());
        options.ctx = ctx;
        options.initConnection = connection -> {
            ctx.withConnection(connection).dao(SqlDao.class).createSchema();
        };
        SqlTesting.runTests(
            options,
            () -> SqlTesting.fromProperties("jdbc:h2:mem:", null, null),
            List.of(SqlDao.class)
        );
    }
}

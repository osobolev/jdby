package jdby.sample.dao;

import jdby.dao.DefaultDaoContext;
import jdby.testing.SqlTesting;
import jdby.testing.TestingOptions;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.List;

public class DaoTest {

    @Test
    public void testDao() throws Exception {
        TestingOptions options = new TestingOptions();
        DefaultDaoContext ctx = new DefaultDaoContext();
        options.ctx = ctx;
        options.initConnection = connection -> {
            ctx.withConnection(connection).dao(UserDao.class).createSchema();
        };
        SqlTesting.runTests(
            options,
            () -> DriverManager.getConnection("jdbc:h2:mem:test"),
            List.of(UserDao.class)
        );
    }
}

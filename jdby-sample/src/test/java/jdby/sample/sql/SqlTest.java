package jdby.sample.sql;

import jdby.testing.SqlTesting;
import jdby.testing.TestingOptions;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.List;

public class SqlTest {

    @Test
    public void testDao() throws Exception {
        TestingOptions options = new TestingOptions();
        options.initConnection = connection -> {
            options.instantiator.create(connection, UserDao.class).createSchema();
        };
        SqlTesting.runTests(
            options,
            () -> DriverManager.getConnection("jdbc:h2:mem:test"),
            List.of(UserDao.class)
        );
    }
}

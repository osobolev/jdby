package sample.sql;

import jdbq.mapping.ColumnNaming;
import jdbq.mapping.DefaultMapperContext;
import jdbq.testing.SqlTesting;
import jdbq.testing.TestingOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SqlTest {

    @Test
    public void testDao() throws Throwable {
        TestingOptions options = new TestingOptions();
        DefaultMapperContext ctx = new DefaultMapperContext(ColumnNaming.camelCase());
        options.ctx = ctx;
        options.initConnection = connection -> {
            new SqlDao(ctx.withConnection(connection)).createSchema();
        };
        SqlTesting.runTests(
            options,
            () -> SqlTesting.fromProperties("jdbc:h2:mem:", null, null),
            List.of(SqlDao.class)
        );
    }
}

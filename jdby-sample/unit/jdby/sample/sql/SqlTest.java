package jdby.sample.sql;

import jdby.mapping.ColumnNaming;
import jdby.mapping.DefaultMapperContext;
import jdby.testing.SqlTesting;
import jdby.testing.TestingOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SqlTest {

    @Test
    public void testDao() throws Exception {
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

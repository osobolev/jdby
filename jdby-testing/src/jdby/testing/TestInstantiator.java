package jdby.testing;

import java.sql.Connection;

public interface TestInstantiator {

    <T> T create(Connection connection, Class<T> cls) throws Exception;
}

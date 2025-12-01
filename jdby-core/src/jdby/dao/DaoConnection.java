package jdby.dao;

import java.sql.Connection;

public interface DaoConnection {

    <T> T dao(Class<T> daoInterface);

    Connection getConnection();
}

package jdby.dao;

import java.sql.Connection;

public interface DaoConnection extends DaoSource {

    Connection getConnection();
}

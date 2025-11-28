package jdby.sample;

import jdby.sample.dao.DaoExample;
import jdby.sample.sql.SqlExample;

import java.sql.SQLException;

public class Example {

    public static void main(String[] args) throws SQLException {
        System.out.println("============== SQL:");
        SqlExample.main(args);
        System.out.println("============== DAO:");
        DaoExample.main(args);
    }
}

package jdby.sample.dao;

import jdby.core.Batch;
import jdby.core.UncheckedSQLException;
import jdby.dao.DaoConnection;
import jdby.dao.DaoContext;
import jdby.dao.DefaultDaoContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@SuppressWarnings({"AutoUnboxing", "UseOfSystemOutOrSystemErr"})
public class DaoExample {

    public static void main(String[] args) throws SQLException {
        DaoContext ctx = new DefaultDaoContext();
        try (Connection jdbcConnection = DriverManager.getConnection("jdbc:h2:mem:")) {
            jdbcConnection.setAutoCommit(false);
            DaoConnection connection = ctx.withConnection(jdbcConnection);

            UserDao dao = connection.dao(UserDao.class);

            dao.createSchema();

            int idNick = dao.insertUser("Nick", LocalDate.of(2000, 12, 8));
            int idJohn = dao.insertUser("John", LocalDate.of(1990, 12, 8));
            System.out.printf("Nick: %s, John: %s%n", idNick, idJohn);

            dao.setLastLogin(idJohn, OffsetDateTime.now());

            List<UserRow> allUsers = dao.listAllUsers();
            System.out.println("All users: " + allUsers);

            List<UserRow> zoomers = dao.listUsersByFilter(null, LocalDate.of(2000, 1, 1), null);
            System.out.println("Zoomers: " + zoomers);

            UserRow nick = dao.maybeUser(idNick);
            System.out.println("Nick: " + nick);

            UserRow unknown = dao.maybeUser(-1);
            System.out.println("Unknown user: " + unknown);

            try {
                dao.loadUser(-1);
            } catch (UncheckedSQLException ex) {
                System.out.println("User not found!");
            }

            try (Batch batch = new Batch(10)) {
                for (int i = 0; i < 20; i++) {
                    dao.batchAddUser(batch, "Test user", null);
                }
            }
            System.out.println(dao.listAllUsers().size());
        }
    }
}

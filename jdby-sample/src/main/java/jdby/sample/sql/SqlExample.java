package jdby.sample.sql;

import jdby.core.Batch;
import jdby.core.RowConnection;
import jdby.core.UncheckedSQLException;
import jdby.mapping.MapperContext;
import jdby.transaction.ConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@SuppressWarnings({"AutoUnboxing", "UseOfSystemOutOrSystemErr"})
public class SqlExample {

    public static void main(String[] args) throws SQLException {
        MapperContext ctx = MapperContext.builder().buildCore();
        try (Connection jdbcConnection = DriverManager.getConnection("jdbc:h2:mem:")) {
            jdbcConnection.setAutoCommit(false);
            RowConnection connection = ctx.withConnection(jdbcConnection);

            UserDao dao = new UserDao(connection);

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
            System.out.println("After batch insert: " + dao.listAllUsers().size());

            // Commit previous transaction:
            connection.getConnection().commit();
            try {
                ConnectionFactory dataSource = ConnectionFactory.fromConnection(jdbcConnection);
                ctx.transaction(dataSource).run(tx -> {
                    UserDao txDao = new UserDao(tx);
                    txDao.deleteAllUsers();
                    throw new IllegalStateException("Should rollback");
                });
            } catch (IllegalStateException ex) {
                System.out.println("After delete rollback: " + dao.listAllUsers().size());
            }
        }
    }
}

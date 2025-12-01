# JDBY (Java + DB = Yay!)

## Motivating example

```java
record UserRow (int id, String name) {}

interface UserDao {

    default List<UserRow> listUsers(String nameMask) {
        return listRows("""
            select id, name
              from users
             where name like :nameMask
            order by name
            """);
    }
}
...
Connection jdbcConnection = ...;
DaoContext ctx = DaoContext.builder().build();
DaoConnection connection = ctx.withConnection(jdbcConnection);

UserDao dao = connection.dao(UserDao.class);
List<UserRow> found = dao.listUsers("%John%");
```

See full example code [here](https://github.com/osobolev/jdby/blob/master/jdby-sample/src/jdby/sample/dao).

## Why not JDBI?

- JDBI's support for **dynamic SQL** in declarative queries relies on hard-to-manage string templates.
- JDBI's support for records does not work out of the box (you need to specify a mapper somewhere).
- **JDBI is too large** for simple SQL execution. For example, the equivalent of the sample at the above link 
requires **1.6 Mb** of dependencies (**1.2 Mb** for JDBI itself + **0.4 Mb** for StringTemplate4 engine)

So if you:
- need only standard SQL execution functionality 
- use dynamic SQL extensively
- don't like bloated software

then you can choose JDBY.

## Use

Maven:
```xml
<dependency>
    <groupId>io.github.osobolev.jdby</groupId>
    <artifactId>jdby-core</artifactId>
    <version>1.0</version>
</dependency>
```

Gradle:
```kotlin
implementation("io.github.osobolev.jdby:jdby-core:1.0")

```

## Limitations

- **Java 17 or newer** is required.
- **Only records** are supported for multi-column queries.
- Query parameters for DAOs are mapped by name, so `-parameter` is required for `javac`.

## Single-column queries

Simple types (primitive type wrappers, `String`, `BigInteger`, `BigDecimal`, `LocalDate`, `LocalTime`, `LocalDateTime`, `OffsetDateTime`, `byte[]`)
are supported for single-column queries:
```java
default List<Integer> listUserIds() {
    return listRows("select id from users order by id");
}
```

## Single-row queries

- `maybeRow` returns null or empty `Optional` when no rows are found; returns the first row when one or more rows exist:
```java
// Returns null if no rows found
default UserRow maybeUser(int userId) {
    return maybeRow("select id, name from users where id = :userId");
}

// Returns Optional.empty() if no rows found
default Optional<UserRow> optionalUser(int userId) {
  return maybeRow("select id, name from users where id = :userId");
}
```
- `exactlyOneRow` throws exception if there are no rows or more than one row:
```java
default UserRow loadUser(int userId) {
    return exactlyOneRow("select id, name from users where id = :userId");
}
```

## Generated columns

You can retrieve the **generated ID** of an inserted row using the `insertRow` method:
```java
default int insertUser(String name) {
    return insertRow(column("id"), "insert into users (name) values (:name)");
}
```

For more complex cases (more than one column or more than one row being inserted/updated) you can use the method `executeUpdate` with the `GeneratedKeyMapper` parameter.

## Dynamic SQL

You can use the `SqlBuilder` class to construct dynamic SQL queries from separate pieces:
```java
default List<UserRow> listUsersByFilter(LocalDate birthdayFrom, LocalDate birthdayTo) {
    SqlBuilder buf = builder("""
        select id, name
          from users
         where 1 = 1
        """);
    if (birthdayFrom != null) {
        buf.append("and dob >= :birthdayFrom");
    }
    if (birthdayTo != null) {
        buf.append("and dob <= :birthdayTo");
    }
    buf.append("order by name");
    return listRows(buf);
}
```

## Batching

You can use query batching in the following way:
```java
interface UserDao {

    default void batchInsertUser(Batch batch, String name) {
        executeBatch(batch, "insert into users (name) values (:name)");
    }
}
...
try (Batch batch = new Batch(10)) {
    for (int i = 0; i < 20; i++) {
        dao.batchInsertUser(batch, "Test user");
    }
}
```

## Mapping of record fields to query columns

- `DaoContext.builder().setColumnNaming()` defines the default mapping strategy. There are three built-in strategies:
    - `ColumnNaming.CamelCase` (**default strategy**): a record field like `fullName` is queried from the DB column `full_name`.
    - `ColumnNaming.Raw`: the record field value is queried from the DB column with the **same name**.
    - `ColumnNaming.ByPosition`: the record field value is queried from the DB column at the **same position** in the column list.
- `SqlName` annotation on a record component overrides the default mapping strategy (but is not used for `ColumnNaming.ByPosition` strategy).
- `SqlNameStrategy` annotation on a record overrides the default mapping strategy for that specific record.

## Testing

Testing is supported by a separate module `jdby-testing`. Run tests this way:
```java
public class DaoTest {

    @Test
    public void testDao() throws Exception {
        TestingOptions options = new TestingOptions();
        // Possibly customize options.ctx
        SqlTesting.runTests(
            options,
            () -> DriverManager.getConnection("jdbc:h2:mem:test"),
            List.of(UserDao.class)
        );
    }
}
```

When testing **all public non-static methods** of the given DAO classes are invoked with mock parameters. Then following checks are then performed:
- for `listRows`/`maybeRow`/`exactlyOneRow`, record fields are compared with query columns. Errors or warnings are reported if there are any **discrepancies** in names or types.
- for `executeUpdate`/`insertRow` any changes are **rolled back**; any constraint violation errors are ignored (because mock values can violate unique/FK constraints).

This ensures that all SQL queries are valid and column-to-record mapping is correct.

## Customization

- `DaoContext.builder().setColumnNaming()` customizes the automatic record-to-column name mapping strategy (e.g., changing from `camelCase` to `snake_case`).
- `DaoContext.builder().registerColumn()` customizes the mapping for an individual column, which applies to both single-column queries and columns within a record.
- `DaoContext.builder().registerRow()` customizes manual row mapping (your only way to use non-record classes as row types).
- `DaoContext.builder().registerParameter()` customizes how a specific Java type is passed as a parameter into an SQL query.

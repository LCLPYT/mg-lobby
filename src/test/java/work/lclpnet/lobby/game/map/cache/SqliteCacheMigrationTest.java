package work.lclpnet.lobby.game.map.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SqliteCacheMigrationTest {

    static final Logger logger = LoggerFactory.getLogger(SqliteCacheMigrationTest.class);
    SqliteCacheMigration cacheMigration;
    Connection connection;

    @BeforeAll
    public static void setupJdbc() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC", true, SqliteCacheMigration.class.getClassLoader());
    }

    @BeforeEach
    void setUp() throws IOException, SQLException {
        Path path = Files.createTempFile("mgl_cm", "index.sqlite");
        String absPath = path.toAbsolutePath().toString();

        connection = DriverManager.getConnection("jdbc:sqlite:".concat(absPath));

        cacheMigration = new SqliteCacheMigration(connection, logger);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void migrate() throws SQLException {
        cacheMigration.migrate();

        Set<String> tableNames = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");

            while (result.next()) {
                String name = result.getString(1);
                tableNames.add(name);
            }
        }

        assertEquals(Set.of("info", "entries"), tableNames);
    }

    @Test
    void isUpToDate_nothing_false() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(SqliteCacheMigration.isUpToDate(statement));
        }
    }

    @Test
    void isUpToDate_noVersion_false() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            SqliteCacheMigration.createInfoTable(statement);
            assertFalse(SqliteCacheMigration.isUpToDate(statement));
        }
    }

    @Test
    void isUpToDate_olderVersion_false() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            SqliteCacheMigration.createInfoTable(statement);

            cacheMigration.updateVersion(SqliteCacheMigration.VERSION - 1);

            assertFalse(SqliteCacheMigration.isUpToDate(statement));
        }
    }

    @Test
    void isUpToDate_upToDate_true() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            SqliteCacheMigration.createInfoTable(statement);

            // write current version
            cacheMigration.updateVersion();

            assertTrue(SqliteCacheMigration.isUpToDate(statement));
        }
    }

    @Test
    void migrate_upToDate_noop() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            SqliteCacheMigration.createInfoTable(statement);
        }

        // write current version
        cacheMigration.updateVersion();

        // now try to migrate the database, this should not be executed
        cacheMigration.migrate();

        Set<String> tableNames = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");

            while (result.next()) {
                String name = result.getString(1);
                tableNames.add(name);
            }
        }

        assertEquals(Set.of("info"), tableNames);
    }

    @Test
    void migrate_partial_migrated() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            SqliteCacheMigration.createInfoTable(statement);
        }

        cacheMigration.migrate();

        Set<String> tableNames = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");

            while (result.next()) {
                String name = result.getString(1);
                tableNames.add(name);
            }
        }

        assertEquals(Set.of("info", "entries"), tableNames);
    }
}
package work.lclpnet.lobby.game.map.cache;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteCacheIndexTest {

    static final Logger logger = LoggerFactory.getLogger(SqliteCacheIndexTest.class);

    @BeforeAll
    static void setupJdbc() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC", true, SqliteCacheIndex.class.getClassLoader());
    }

    @Test
    void getTimestamp_exists() throws SQLException, IOException {
        Path path = Path.of("src", "test", "resources", "cache", "index_simple_entry.sqlite");

        try (var index = SqliteCacheIndex.createSqliteIndex(path, logger)) {
            assertEquals(1720210276, index.getTimestamp("test/hello"));
        }
    }

    @Test
    void isEntryInvalid_withinTtl_false() throws SQLException, IOException {
        Path path = Path.of("src", "test", "resources", "cache", "index_simple_entry.sqlite");

        long ttl = System.currentTimeMillis() / 1000 - 1720210276 + 10;

        try (var index = SqliteCacheIndex.createSqliteIndex(path, logger)) {
            assertFalse(index.isEntryInvalid("test/hello", (int) ttl));
        }
    }

    @Test
    void isEntryInvalid_tooOld_true() throws SQLException, IOException {
        Path path = Path.of("src", "test", "resources", "cache", "index_simple_entry.sqlite");

        try (var index = SqliteCacheIndex.createSqliteIndex(path, logger)) {
            assertTrue(index.isEntryInvalid("test/hello", 60));
        }
    }

    @Nested
    class Fresh {
        SqliteCacheIndex index;

        @BeforeEach
        void setUp() throws IOException, SQLException {
            Path path = Files.createTempFile("mgl_sci", "index.sqlite");

            // this executes SqliteCacheMigration internally
            index = SqliteCacheIndex.createSqliteIndex(path, logger);
        }

        @AfterEach
        void tearDown() throws IOException {
            index.close();
        }

        @Test
        void getTimestamp() {
            assertEquals(-1, index.getTimestamp("test/hello"));
        }

        @Test
        void updateEntry() {
            String path = "test/hello";

            long now = System.currentTimeMillis() / 1000;

            index.updateEntry(path);

            long time = index.getTimestamp(path);

            assertTrue(time >= now);
        }

        @Test
        void isEntryInvalid_doesNotExist_true() {
            assertTrue(index.isEntryInvalid("test/hello", 60));
        }
    }
}
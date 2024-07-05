package work.lclpnet.lobby.game.map.cache;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteCacheIndex implements CacheIndex {

    private final Connection connection;
    private final Logger logger;

    public SqliteCacheIndex(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IOException("Failed to close SQL connection", e);
        }
    }

    @Override
    public boolean isEntryInvalid(String path, int ttlSeconds) {
        try (var statement = connection.prepareStatement("SELECT timestamp FROM entries WHERE path = ?")) {

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return true;

                long timestamp = result.getLong(0);

                return System.currentTimeMillis() - timestamp >= ttlSeconds * 1000L;
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch cache entry timestamp", e);
            return true;
        }
    }

    public static SqliteCacheIndex createSqliteIndex(Path path, Logger logger) throws SQLException {
        Connection connection = createSqliteConnection(path, logger);

        return new SqliteCacheIndex(connection, logger);
    }

    public static Connection createSqliteConnection(Path path, Logger logger) throws SQLException {
        String absPath = path.toAbsolutePath().toString();

        logger.info("Trying to connect to SQLite database at {}", path);

        Connection connection = DriverManager.getConnection("jdbc:sqlite:".concat(absPath));

        logger.info("Successfully established SQLite connection");

        SqliteCacheMigration migration = new SqliteCacheMigration(connection, logger);
        migration.migrate();

        return connection;
    }
}

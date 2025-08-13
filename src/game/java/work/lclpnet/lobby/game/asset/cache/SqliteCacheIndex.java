package work.lclpnet.lobby.game.asset.cache;

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
    public boolean isEntryInvalid(String path) {
        long expiryTimestamp = getExpiryTimestamp(path);

        if (expiryTimestamp == -1) return true;

        return System.currentTimeMillis() / 1000 >= expiryTimestamp;
    }

    /**
     * Gets the expiry timestamp in seconds (unix timestamp) for a path.
     * @param path The path.
     * @return The expiry timestamp, or -1 if not set.
     */
    public long getExpiryTimestamp(String path) {
        try (var statement = connection.prepareStatement("SELECT expiry FROM entries WHERE path = ?")) {
            statement.setString(1, path);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return -1;

                return result.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch cache entry timestamp", e);
            return -1;
        }
    }

    @Override
    public void updateEntry(String path, int ttlSeconds) {
        try (var statement = connection.prepareStatement("REPLACE INTO entries (path, expiry) VALUES (?, ?)")) {
            statement.setString(1, path);
            statement.setLong(2, System.currentTimeMillis() / 1000 + ttlSeconds);

            statement.execute();
        } catch (SQLException e) {
            logger.error("Failed to update cache entry timestamp", e);
        }
    }

    @Override
    public void invalidate(String path) {
        try (var statement = connection.prepareStatement("DELETE FROM entries WHERE path = ?")) {
            statement.setString(1, path);

            statement.execute();
        } catch (SQLException e) {
            logger.error("Failed to invalidate cache entry", e);
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

package work.lclpnet.lobby.game.asset.cache;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.sql.*;

public class SqliteCacheMigration {

    public static final int VERSION = 0;
    private final Connection connection;
    private final Logger logger;

    public SqliteCacheMigration(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public void migrate() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (isUpToDate(statement)) return;

            logger.info("Migrating cache...");

            createInfoTable(statement);
            createEntriesTable(statement);

            updateVersion();

            logger.info("Cache migrated successfully");
        }
    }

    @VisibleForTesting
    static boolean isUpToDate(Statement statement) {
        try (ResultSet result = statement.executeQuery("SELECT version FROM info LIMIT 1")) {
            if (!result.next()) return false;

            int version = result.getInt(1);

            return version >= VERSION;
        } catch (SQLException e) {
            return false;
        }
    }

    @VisibleForTesting
    static void createInfoTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS info (version int unsigned NOT NULL)");
    }

    @VisibleForTesting
    void updateVersion() throws SQLException {
        updateVersion(VERSION);
    }

    @VisibleForTesting
    void updateVersion(int version) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("REPLACE INTO info (version) VALUES (?)");

        try (statement) {
            statement.setInt(1, version);
            statement.execute();
        }
    }

    private static void createEntriesTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS entries (\
                path string NOT NULL PRIMARY KEY, \
                expiry bigint unsigned NOT NULL
                )""");
    }
}

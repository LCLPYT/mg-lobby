package work.lclpnet.lobby.game.map.cache;

import org.slf4j.Logger;

import java.sql.*;

public class CacheMigration {

    public static final int VERSION = 0;
    private final Connection connection;
    private final Logger logger;

    public CacheMigration(Connection connection, Logger logger) {
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

    private static boolean isUpToDate(Statement statement) {
        try (ResultSet result = statement.executeQuery("SELECT version FROM info LIMIT 1")) {
            if (!result.next()) return false;

            int version = result.getInt(0);

            return version >= VERSION;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createInfoTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS info (version int unsigned NOT NULL)");
    }

    private void updateVersion() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("REPLACE INTO info (version) VALUES (?)");

        try (statement) {
            statement.setInt(1, VERSION);
            statement.execute();
        }
    }

    private static void createEntriesTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS entries (\
                path string NOT NULL PRIMARY KEY, \
                timestamp bigint unsigned NOT NULL
                )""");
    }
}

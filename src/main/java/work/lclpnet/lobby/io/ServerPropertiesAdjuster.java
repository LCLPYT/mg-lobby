package work.lclpnet.lobby.io;

import org.slf4j.Logger;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.LobbyConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ServerPropertiesAdjuster {

    private final Path path;
    private final ConfigAccess configAccess;
    private final Logger logger;
    private final Properties properties = new Properties();

    @Inject
    public ServerPropertiesAdjuster(@Named("serverProperties") Path path, ConfigAccess configAccess, Logger logger) {
        this.path = path;
        this.configAccess = configAccess;
        this.logger = logger;
    }

    public void adjust() {
        LobbyConfig config = configAccess.getConfig();
        String levelName = config.getSafeLobbyLevelName();

        try {
            load();

            if (properties.contains("level-name") && levelName.equals(properties.get("level-name"))) return;

            properties.put("level-name", levelName);

            store();
        } catch (IOException e) {
            logger.error("Could not adjust {}", path, e);
        }
    }

    private void load() throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path);
            return;
        }

        properties.clear();

        try (var in = Files.newInputStream(path)) {
            properties.load(in);
        }
    }

    private void store() throws IOException {
        try (var out = Files.newOutputStream(path)) {
            properties.store(out, "Minecraft server properties");
        }
    }
}

package work.lclpnet.lobby;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.config.json.FileConfigSerializer;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.LobbyConfig;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class LobbyManagerImpl implements LobbyManager {

    private final ConfigHandler<LobbyConfig> configHandler;
    private final PluginContext pluginContext;
    private final Logger logger;

    public LobbyManagerImpl(PluginContext pluginContext, Logger logger) {
        this.pluginContext = pluginContext;
        this.logger = logger;

        var configSerializer = new FileConfigSerializer<>(LobbyConfig.FACTORY, logger);
        Path configFile = FabricLoader.getInstance().getConfigDir()
                .resolve(LobbyPlugin.ID).resolve("config.json");

        this.configHandler = new ConfigHandler<>(configFile, configSerializer, logger);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Nonnull
    @Override
    public LobbyConfig getConfig() {
        return configHandler.getConfig();
    }

    public void init() {
        configHandler.loadConfig();
    }
}

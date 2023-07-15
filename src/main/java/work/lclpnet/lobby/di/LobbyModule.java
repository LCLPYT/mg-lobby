package work.lclpnet.lobby.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.LobbyManagerImpl;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;

@Module(includes = LobbyModule.Bindings.class)
public class LobbyModule {

    @Module
    interface Bindings {
        @Binds
        LobbyManager bindLobbyManager(LobbyManagerImpl impl);

        @Binds
        ConfigAccess bindConfigAccess(LobbyManager impl);
    }

    private final Logger logger;
    private final TranslationService translationService;
    private final PluginContext pluginContext;

    public LobbyModule(Logger logger, TranslationService translationService, PluginContext pluginContext) {
        this.logger = logger;
        this.translationService = translationService;
        this.pluginContext = pluginContext;
    }

    @Provides
    Logger provideLogger() {
        return logger;
    }

    @Provides
    TranslationService provideTranslationService() {
        return translationService;
    }

    @Provides
    PluginContext providePluginContext() {
        return pluginContext;
    }

    @Singleton
    @Provides
    ConfigHandler<LobbyConfig> provideConfigHandler() {
        var configSerializer = new ExtendedConfigSerializer<>(LobbyConfig.FACTORY, logger);
        var configFile = FabricLoader.getInstance().getConfigDir().resolve(LobbyPlugin.ID).resolve("config.json");

        return new ConfigHandler<>(configFile, configSerializer, logger);
    }

    @Provides
    LobbyConfig provideLobbyConfig(ConfigAccess configAccess) {
        return configAccess.getConfig();
    }

    @Provides @Named("serverProperties")
    Path provideServerPropertiesPath() {
        return Path.of("server.properties");
    }

    @Provides @Named("lobbyWorld")
    ServerWorld provideServerWorld(LobbyManager lobbyManager) {
        return lobbyManager.getLobbyWorld();
    }
}

package work.lclpnet.lobby.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.BlockView;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.LobbyManagerImpl;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.game.AsyncGameStateIo;
import work.lclpnet.lobby.game.GameStateIo;
import work.lclpnet.lobby.game.api.data.DataPackSink;
import work.lclpnet.lobby.game.impl.data.PathDataPackSink;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Module(includes = LobbyModule.Bindings.class)
public class LobbyModule {

    @Module
    interface Bindings {
        @Binds
        LobbyManager bindLobbyManager(LobbyManagerImpl impl);

        @Binds
        ConfigAccess bindConfigAccess(LobbyManager impl);

        @Binds
        BlockView bindBlockView(@Named("lobbyWorld") ServerWorld impl);

        @Binds
        GameStateIo bindGameManagerStateManager(AsyncGameStateIo impl);
    }

    private final Logger logger;
    private final Translations translations;
    private final Future<MinecraftServer> server;

    public LobbyModule(Logger logger, Translations translations, Future<MinecraftServer> server) {
        this.logger = logger;
        this.translations = translations;
        this.server = server;
    }

    @Provides
    Logger provideLogger() {
        return logger;
    }

    @Provides
    Translations provideTranslations() {
        return translations;
    }

    @Provides
    Future<MinecraftServer> provideServerFuture() {
        return server;
    }

    @Provides
    MinecraftServer provideServer() {
        return server.resultNow();
    }

    @Singleton
    @Provides
    ConfigHandler<LobbyConfig> provideConfigHandler() {
        var configSerializer = new ExtendedConfigSerializer<>(LobbyConfig.FACTORY, logger);
        var configFile = FabricLoader.getInstance().getConfigDir().resolve(LobbyMod.ID).resolve("config.json");

        return new ConfigHandler<>(configFile, configSerializer, logger);
    }

    @Provides
    LobbyConfig provideLobbyConfig(ConfigAccess configAccess) {
        return configAccess.getConfig();
    }

    @Provides
    LobbyWorldConfig provideWorldConfig(ConfigAccess configAccess) {
        return configAccess.getWorldConfig();
    }

    @Provides @Named("serverProperties")
    Path provideServerPropertiesPath() {
        return Path.of("server.properties");
    }

    @Provides @Named("gameManagerStatePath")
    Path provideGameManagerStatePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(LobbyMod.ID).resolve("gameManagerState.dat");
    }

    @Provides @Named("lobbyWorld")
    ServerWorld provideServerWorld(LobbyManager lobbyManager) {
        return lobbyManager.getLobbyWorld();
    }

    @Provides @Named("dataPacks")
    Path provideDataPacksPath(ConfigAccess configAccess) {
        return Path.of(configAccess.getConfig().getSafeLobbyLevelName()).resolve("datapacks");
    }

    @Provides @Singleton
    Executor provideIoExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Provides
    DataPackSink provideDataPackSink(@Named("dataPacks") Path path) {
        return new PathDataPackSink(path);
    }
}

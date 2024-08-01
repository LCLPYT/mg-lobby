package work.lclpnet.lobby;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.config.WorldConfigHandler;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.service.PalService;
import work.lclpnet.lobby.util.PlayerReset;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.concurrent.Future;

@Singleton
public class LobbyManagerImpl implements LobbyManager {

    private final ConfigHandler<LobbyConfig> configHandler;
    private final Logger logger;
    private final Translations translations;
    private final GameManager gameManager;
    private final Future<MinecraftServer> server;
    private volatile WorldConfigHandler<LobbyWorldConfig> worldConfigHandler = null;

    @Inject
    public LobbyManagerImpl(Translations translations, Logger logger,
                            GameManager gameManager, ConfigHandler<LobbyConfig> configHandler, Future<MinecraftServer> server) {
        this.logger = logger;
        this.translations = translations;
        this.gameManager = gameManager;
        this.configHandler = configHandler;
        this.server = server;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    @Override
    public LobbyConfig getConfig() {
        LobbyConfig config = configHandler.getConfig();
        if (config == null) throw new IllegalStateException("Config not loaded");

        return config;
    }

    @SuppressWarnings("resource")
    @Override
    public ServerWorld getLobbyWorld() {
        return this.server.resultNow().getOverworld();
    }

    @Override
    public Vec3d getLobbySpawn() {
        BlockPos spawnPos = getLobbyWorld().getSpawnPos();

        return new Vec3d(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5
        );
    }

    @Override
    public void sendToLobby(ServerPlayerEntity player) {
        final ServerWorld world = getLobbyWorld();
        final Vec3d spawn = getLobbySpawn();

        PlayerReset.reset(player);

        player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), 0F, 0F);
    }

    @Override
    public Translations getTranslations() {
        return translations;
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    @NotNull
    public LobbyWorldConfig getWorldConfig() {
        final var worldConfigHandler = this.worldConfigHandler;

        if (worldConfigHandler == null) {
            throw new IllegalStateException("Server not loaded");
        }

        final LobbyWorldConfig config = worldConfigHandler.getConfig();

        if (config == null) {
            throw new IllegalStateException("World config not loaded");
        }

        return config;
    }

    public void init() {
        configHandler.loadConfig();
    }

    public void onWorldReady() {
        ServerWorld world = getLobbyWorld();
        var serializer = new ExtendedConfigSerializer<>(LobbyWorldConfig.factory(world.getRegistryManager()), logger);
        Path path = Path.of("config", "lobby.json");

        worldConfigHandler = new WorldConfigHandler<>(world, path, serializer, logger);
        worldConfigHandler.loadConfig();

        configurePal();
    }

    private void configurePal() {
        if (!FabricLoader.getInstance().isModLoaded("pal")) return;

        try {
            Class.forName("work.lclpnet.pal.PalApi", false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.warn("Could not find class 'work.lclpnet.pal.PalApi', although pal is installed. Perhaps this version is incompatible with mg-lobby?");
            return;
        }

        PalService.configurePal();
    }
}

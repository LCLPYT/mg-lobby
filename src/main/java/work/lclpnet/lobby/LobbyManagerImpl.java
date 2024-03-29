package work.lclpnet.lobby;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.kibu.hook.util.PlayerUtils;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.config.WorldConfigHandler;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.service.PalService;
import work.lclpnet.mplugins.MPluginsAPI;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Optional;

@Singleton
public class LobbyManagerImpl implements LobbyManager {

    private final ConfigHandler<LobbyConfig> configHandler;
    private final PluginContext pluginContext;
    private final Logger logger;
    private final TranslationService translationService;
    private final GameManager gameManager;
    private volatile WorldConfigHandler<LobbyWorldConfig> worldConfigHandler = null;

    @Inject
    public LobbyManagerImpl(PluginContext pluginContext, TranslationService translationService, Logger logger,
                            GameManager gameManager, ConfigHandler<LobbyConfig> configHandler) {
        this.pluginContext = pluginContext;
        this.logger = logger;
        this.translationService = translationService;
        this.gameManager = gameManager;
        this.configHandler = configHandler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Nonnull
    @Override
    public LobbyConfig getConfig() {
        LobbyConfig config = configHandler.getConfig();
        if (config == null) throw new IllegalStateException("Config not loaded");

        return config;
    }

    @Override
    public ServerWorld getLobbyWorld() {
        MinecraftServer server = pluginContext.getEnvironment().getServer();
        return server.getOverworld();
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

        resetPlayer(player);

        player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), 0F, 0F);
    }

    private void resetPlayer(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.ADVENTURE);
        player.clearStatusEffects();
        player.getInventory().clear();
        PlayerUtils.setCursorStack(player, ItemStack.EMPTY);

        player.getHungerManager().setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(0F);
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
        player.setFireTicks(0);
        player.setOnFire(false);

        Optional.ofNullable(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH))
                .ifPresent(attribute -> attribute.setBaseValue(20));

        player.dismountVehicle();

        PlayerAbilities abilities = player.getAbilities();
        abilities.flying = false;
        abilities.allowFlying = false;
        abilities.invulnerable = false;
        abilities.setFlySpeed(0.05f);
        abilities.setWalkSpeed(0.1f);
    }

    @Override
    public TranslationService getTranslationService() {
        return translationService;
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
        var serializer = new ExtendedConfigSerializer<>(LobbyWorldConfig.FACTORY, logger);
        Path path = Path.of("config", "lobby.json");

        worldConfigHandler = new WorldConfigHandler<>(world, path, serializer, logger);
        worldConfigHandler.loadConfig();

        gameManager.reload();

        configurePal();
    }

    private void configurePal() {
        if (!MPluginsAPI.get().getPluginFrame().getPluginManager().isPluginLoaded("pal")) return;

        try {
            Class.forName("work.lclpnet.pal.PalApi", false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.warn("Could not find class 'work.lclpnet.pal.PalApi', although pal is installed. Perhaps this version is incompatible with mg-lobby?");
            return;
        }

        PalService.configurePal();
    }
}

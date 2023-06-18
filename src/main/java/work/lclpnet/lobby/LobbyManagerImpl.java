package work.lclpnet.lobby;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.kibu.hook.util.PlayerUtils;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.api.LobbyManager;
import work.lclpnet.lobby.config.ExtendedConfigSerializer;
import work.lclpnet.lobby.config.LobbyConfig;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.service.TranslationService;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.translation.SPITranslationLoader;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Optional;

public class LobbyManagerImpl implements LobbyManager {

    private final ConfigHandler<LobbyConfig> configHandler;
    private final PluginContext pluginContext;
    private final Logger logger;
    private final DefaultLanguageTranslator translator;
    private final TranslationService translationService;
    private final GameManager gameManager;

    public LobbyManagerImpl(PluginContext pluginContext, Logger logger) {
        this.pluginContext = pluginContext;
        this.logger = logger;

        var configSerializer = new ExtendedConfigSerializer<>(LobbyConfig.FACTORY, logger);
        Path configFile = FabricLoader.getInstance().getConfigDir()
                .resolve(LobbyPlugin.ID).resolve("config.json");

        this.configHandler = new ConfigHandler<>(configFile, configSerializer, logger);

        SPITranslationLoader translationLoader = new SPITranslationLoader(getClass().getClassLoader());
        this.translator = new DefaultLanguageTranslator(translationLoader);

        this.translationService = new TranslationService(translator);

        this.gameManager = new GameManager(logger);
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
        player.changeGameMode(GameMode.SURVIVAL);
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

    public void init() {
        configHandler.loadConfig();
        translator.reload().join();
    }

    public void loadGames() {
        gameManager.reload();
    }
}

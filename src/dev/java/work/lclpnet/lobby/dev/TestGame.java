package work.lclpnet.lobby.dev;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.game.api.Game;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.data.GameDataPacks;
import work.lclpnet.game.api.start.GameStartScope;
import work.lclpnet.game.api.start.GameStatusManager;
import work.lclpnet.game.impl.MinecraftGameConfig;
import work.lclpnet.game.util.GameStartUtil;

/**
 * Descriptor of a minigame.
 * An example of how to restrict the start condition, configure messages, providing a GameFactory and provide datapacks.
 */
public class TestGame implements Game {

    public static final String MOD_ID = "mg-lobby-dev";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public @NotNull GameConfig getConfig() {
        return new MinecraftGameConfig("test", new ItemStackTemplate(Items.STRUCTURE_VOID));
    }

    @Override
    public boolean canBePlayed(@NonNull GameStartScope scope) {
        // some condition that needs to be met before this game can be started
        return scope.playerCount() >= getRequiredPlayers();
    }

    @Override
    public void configureStatusManager(@NotNull GameStatusManager manager) {
        // optional configuration of messages
        GameStartUtil.configureNotEnoughPlayersMessage(manager, getRequiredPlayers());
        GameStartUtil.configureWaitingForPlayersBossBar(manager);
    }

    private int getRequiredPlayers() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? 1 : 2;
    }

    @Override
    public @NonNull GameFactory createFactory() {
        // Required, handles game translation loader and creates the game instance
        // GameFactory can also configure an activity to start while the game is selected by the lobby, such as a map voting or team selection.
        // If no activity is needed, you can use the provided ModGameFactory for convenience (see AnotherGame.java)
        return new TestGameFactory(logger);
    }

    @Override
    public @NonNull GameDataPacks getBootstrapDataPacks() {
        // optional, use this if the game requires data packs that must be loaded at bootstrap (e.g. world generators, biomes, other static registry data)
        return new TestGameDataPacks();
    }
}

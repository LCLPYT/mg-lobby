package work.lclpnet.lobby.dev;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.GameConfigurator;
import work.lclpnet.lobby.game.api.option.GameOptionConfig;
import work.lclpnet.lobby.game.api.option.OptionVoting;
import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.api.GameConfig;
import work.lclpnet.lobby.game.api.start.GameScope;
import work.lclpnet.lobby.game.api.start.GameStatusManager;
import work.lclpnet.lobby.game.impl.MinecraftGameConfig;
import work.lclpnet.lobby.game.impl.ModGameFactory;

import java.util.List;

public class TestGame implements Game, GameConfigurator {

    public static final String MOD_ID = "mg-lobby-dev";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("test", new ItemStack(Items.STRUCTURE_VOID));
    }

    @Override
    public boolean canBePlayed(GameScope scope) {
        return scope.playerCount() >= getRequiredPlayers();
    }

    @Override
    public GameFactory createFactory() {
        // will be called each time this game is selected to be played
        return new ModGameFactory(MOD_ID, logger, TestGameInstance::new);
    }

    // optional, use this if the game requires data packs that must be loaded at bootstrap (e.g. world generators, biomes, other static registry data)
    @Override
    public GameDataPacks getBootstrapDataPacks() {
        return new TestGameDataPacks();
    }

    // optional, use this if your game has configurable options, such as map-votings etc.
    @Override
    public void configureOptions(GameOptionConfig config) {
        config.registerVoting("map", new OptionVoting<>(
                player -> {
                    var stack = new ItemStack(Items.PAPER);
                    stack.set(DataComponentTypes.ITEM_NAME, Text.literal("Map Voting"));

                    return stack;
                },
                player -> Text.literal("Map"),
                String.class,
                List.of("Map A", "Map B", "Map C"),
                (player, map) -> {
                    var stack = new ItemStack(switch (map) {
                        case "Map A" -> Items.DIAMOND;
                        case "Map B" -> Items.EMERALD;
                        case "Map C" -> Items.DIRT;
                        default -> Items.STRUCTURE_VOID;
                    });

                    stack.set(DataComponentTypes.ITEM_NAME, Text.literal(map).formatted(Formatting.GREEN));

                    return stack;
                })
        );
    }

    @Override
    public void configureStatusManager(GameStatusManager manager) {
        Translations translations = manager.getContext().getTranslations();

        // you can set a periodic condition message that gets sent to everyone, if the game cannot start.
        var notEnoughPlayers = translations.translateText("lobby.game.not_enough_players", getRequiredPlayers())
                .formatted(Formatting.RED);

        manager.setCannotStartMessage(notEnoughPlayers::translateFor);

        // you can set a title that gets displayed in the boss bar if the game cannot start
        manager.setCannotStartBossBarValue(translations.translateText("lobby.game.waiting_for_players"));
    }

    private int getRequiredPlayers() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? 1 : 2;
    }
}

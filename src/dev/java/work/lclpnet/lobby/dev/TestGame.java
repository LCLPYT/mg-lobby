package work.lclpnet.lobby.dev;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.game.api.Game;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.game.api.GameConfigurator;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.data.GameDataPacks;
import work.lclpnet.game.api.option.GameOptionConfig;
import work.lclpnet.game.api.option.OptionVoting;
import work.lclpnet.game.api.start.GameScope;
import work.lclpnet.game.api.start.GameStatusManager;
import work.lclpnet.game.impl.MinecraftGameConfig;
import work.lclpnet.game.impl.ModGameFactory;

import java.util.List;

import static work.lclpnet.kibu.scheduler.Ticks.seconds;

public class TestGame implements Game, GameConfigurator {

    public static final String MOD_ID = "mg-lobby-dev";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public @NotNull GameConfig getConfig() {
        return new MinecraftGameConfig("test", new ItemStackTemplate(Items.STRUCTURE_VOID));
    }

    @Override
    public boolean canBePlayed(@NonNull GameScope scope) {
        return scope.playerCount() >= getRequiredPlayers();
    }

    @Override
    public @NonNull GameFactory createFactory() {
        // will be called each time this game is selected to be played
        return new ModGameFactory(MOD_ID, logger, TestGameInstance::new);
    }

    // optional, use this if the game requires data packs that must be loaded at bootstrap (e.g. world generators, biomes, other static registry data)
    @Override
    public @NonNull GameDataPacks getBootstrapDataPacks() {
        return new TestGameDataPacks();
    }

    // optional, use this if your game has configurable options, such as map-votings etc.
    @Override
    public void configureOptions(@NotNull GameOptionConfig config) {
        config.registerVoting("map", new OptionVoting<>(
                _ -> {
                    var stack = new ItemStack(Items.PAPER);
                    stack.set(DataComponents.ITEM_NAME, Component.literal("Map Voting"));

                    return stack;
                },
                _ -> Component.literal("Map"),
                String.class,
                List.of("Map A", "Map B", "Map C"),
                (_, map) -> {
                    var stack = new ItemStack(switch (map) {
                        case "Map A" -> Items.DIAMOND;
                        case "Map B" -> Items.EMERALD;
                        case "Map C" -> Items.DIRT;
                        default -> Items.STRUCTURE_VOID;
                    });

                    stack.set(DataComponents.ITEM_NAME, Component.literal(map).withStyle(ChatFormatting.GREEN));

                    return stack;
                })
        ).openBeforeStart(seconds(15));
    }

    @Override
    public void configureStatusManager(@NotNull GameStatusManager manager) {
        Translations translations = manager.getContext().getTranslations();

        // you can set a periodic condition message that gets sent to everyone, if the game cannot start.
        var notEnoughPlayers = translations.translateText("lobby.game.not_enough_players", getRequiredPlayers())
                .formatted(ChatFormatting.RED);

        manager.setCannotStartMessage(notEnoughPlayers::translateFor);

        // you can set a title that gets displayed in the boss bar if the game cannot start
        manager.setCannotStartBossBarValue(translations.translateText("lobby.game.waiting_for_players"));
    }

    private int getRequiredPlayers() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? 1 : 2;
    }
}

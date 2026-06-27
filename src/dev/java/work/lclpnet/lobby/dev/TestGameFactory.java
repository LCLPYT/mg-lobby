package work.lclpnet.lobby.dev;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import work.lclpnet.activity.Activity;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.GameInstance;
import work.lclpnet.game.api.option.OptionVoting;
import work.lclpnet.game.api.option.VoteResult;
import work.lclpnet.game.api.start.GameStartArgs;
import work.lclpnet.game.impl.Voting;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.translations.loader.TranslationLoader;

import java.util.List;

/**
 * An example {@link GameFactory} that provides a map voting functionality using the game selected activity.
 */
public class TestGameFactory implements GameFactory {

    private final Logger logger;

    private @Nullable Voting<String> voting = null;

    public TestGameFactory(Logger logger) {
        this.logger = logger;
    }

    private @NonNull ItemStack getOptionStack(String map) {
        var stack = new ItemStack(switch (map) {
            case "Map A" -> Items.DIAMOND;
            case "Map B" -> Items.EMERALD;
            case "Map C" -> Items.DIRT;
            default -> Items.STRUCTURE_VOID;
        });

        stack.set(DataComponents.ITEM_NAME, Component.literal(map).withStyle(ChatFormatting.GREEN));

        return stack;
    }

    private @NonNull ItemStack getVotingStack() {
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.ITEM_NAME, Component.literal("Map Voting"));

        return stack;
    }

    @Override
    public @Nullable TranslationLoader createTranslationLoader() {
        return ModTranslations.assetTranslationLoader(TestGame.MOD_ID, logger);
    }

    @Override
    public @Nullable Activity createGameSelectedActivity(@NonNull GameStartArgs args) {
        voting = new Voting<>(
                "map",
                new OptionVoting<>(
                        _ -> getVotingStack(),
                        _ -> Component.literal("Map"),
                        String.class,
                        List.of("Map A", "Map B", "Map C"),
                        (_, map) -> getOptionStack(map)
                ),
                args.options().getContext().getTranslations(),
                true,
                true,
                true
        );

        // this activity will take care of setting up the voting (item, vote menu etc.)
        return new TestGameStartingActivity(args, logger, voting);
    }

    @Override
    public @NotNull GameInstance createInstance(@NotNull GameEnvironment environment) {
        // will be called by the game runtime when the game should be started
        VoteResult<String> voteResult = voting != null ? voting.getCurrentResult() : VoteResult.empty();

        return new TestGameInstance(environment, voteResult);
    }
}

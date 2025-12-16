package work.lclpnet.lobby.cmd.arg;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import work.lclpnet.lobby.game.GameManager;

import java.util.concurrent.CompletableFuture;

public class GameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    private final GameManager gameManager;

    public GameSuggestionProvider(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var candidates = gameManager.getGames().stream().map(game -> game.getConfig().identifier());

        SharedSuggestionProvider.suggest(candidates, builder);
        builder.suggest(GameManager.EMPTY_GAME_ID);

        return builder.buildFuture();
    }
}

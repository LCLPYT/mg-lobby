package work.lclpnet.lobby.cmd.arg;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import work.lclpnet.lobby.game.GameManager;

import java.util.concurrent.CompletableFuture;

public class GameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    private final GameManager gameManager;

    public GameSuggestionProvider(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        var candidates = gameManager.getGames().stream().map(game -> game.getConfig().identifier());

        CommandSource.suggestMatching(candidates, builder);
        builder.suggest(GameManager.EMPTY_GAME_ID);

        return builder.buildFuture();
    }
}

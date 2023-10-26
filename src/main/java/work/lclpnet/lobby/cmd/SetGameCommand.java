package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.kibu.plugin.cmd.KibuCommand;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.cmd.arg.GameSuggestionProvider;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.lobby.game.api.Game;

import java.util.function.Consumer;

public class SetGameCommand implements KibuCommand {

    private final GameManager gameManager;
    private final Consumer<Game> consumer;
    private final Logger logger;
    private final TranslationService translations;

    public SetGameCommand(GameManager gameManager, Consumer<Game> consumer, Logger logger,
                          TranslationService translations) {
        this.gameManager = gameManager;
        this.consumer = consumer;
        this.logger = logger;
        this.translations = translations;
    }

    @Override
    public void register(CommandRegistrar commands) {
        commands.registerCommand(commands());
    }

    private LiteralArgumentBuilder<ServerCommandSource> commands() {
        return CommandManager.literal("setgame")
                .requires(s -> s.hasPermissionLevel(2))
                .then(CommandManager.argument("game", StringArgumentType.string())
                        .suggests(new GameSuggestionProvider(gameManager))
                        .executes(this::setGame));
    }

    private int setGame(CommandContext<ServerCommandSource> ctx) {
        String gameId = StringArgumentType.getString(ctx, "game");
        Game game = gameManager.getGame(gameId);

        Text title;

        if (game == null) {
            title = Text.literal("None").formatted(Formatting.YELLOW);
        } else {
            title = translations.translateText("en_us", game.getConfig().titleKey())
                    .formatted(Formatting.YELLOW);
        }

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("Set the current game to ").formatted(Formatting.GRAY))
                .append(title));

        ctx.getSource().getServer()
                .submit(() -> consumer.accept(game))
                .exceptionally(throwable -> {
                    logger.error("Failed to change map", throwable);
                    return null;
                });

        return 0;
    }
}

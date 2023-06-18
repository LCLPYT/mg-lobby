package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.lobby.cmd.arg.GameSuggestionProvider;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameManager;

import java.util.function.Consumer;

public class SetGameCommand {

    private final GameManager gameManager;
    private final Consumer<Game> consumer;

    public SetGameCommand(GameManager gameManager, Consumer<Game> consumer) {
        this.gameManager = gameManager;
        this.consumer = consumer;
    }

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

        String title = game != null ? game.getConfig().title() : "None";

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("Set the current game to ").formatted(Formatting.GRAY))
                .append(Text.literal(title).formatted(Formatting.YELLOW)));

        consumer.accept(game);

        return 0;
    }
}

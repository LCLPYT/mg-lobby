package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.cmd.type.KibuCommand;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.cmd.arg.GameSuggestionProvider;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.game.api.Game;

import java.util.function.Consumer;

public class SetGameCommand implements KibuCommand {

    private final GameManager gameManager;
    private final Consumer<Game> consumer;
    private final Logger logger;
    private final Translations translations;

    public SetGameCommand(GameManager gameManager, Consumer<Game> consumer, Logger logger,
                          Translations translations) {
        this.gameManager = gameManager;
        this.consumer = consumer;
        this.logger = logger;
        this.translations = translations;
    }

    @Override
    public void register(CommandRegistrar commands) {
        commands.registerCommand(commands());
    }

    private LiteralArgumentBuilder<CommandSourceStack> commands() {
        return Commands.literal("setgame")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("game", StringArgumentType.string())
                        .suggests(new GameSuggestionProvider(gameManager))
                        .executes(this::setGame));
    }

    private int setGame(CommandContext<CommandSourceStack> ctx) {
        String gameId = StringArgumentType.getString(ctx, "game");
        Game game = gameManager.getGame(gameId);

        Component title;

        if (game == null) {
            title = Component.literal("None").withStyle(ChatFormatting.YELLOW);
        } else {
            title = translations.translateText("en_us", game.getConfig().titleKey())
                    .formatted(ChatFormatting.YELLOW);
        }

        ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal("Set the current game to ").withStyle(ChatFormatting.GRAY))
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

package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.kibu.plugin.cmd.KibuCommand;
import work.lclpnet.lobby.game.start.GameStarter;

import java.util.function.Supplier;

public class ResumeCommand implements KibuCommand {

    private final Supplier<GameStarter> gameStarterSupplier;

    public ResumeCommand(Supplier<GameStarter> gameStarterSupplier) {
        this.gameStarterSupplier = gameStarterSupplier;
    }

    @Override
    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("resume")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(this::resume);
    }

    private int resume(CommandContext<ServerCommandSource> ctx) {
        GameStarter starter = gameStarterSupplier.get();

        if (starter == null) {
            ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                    .append(Text.literal("There is no game starting at the moment").formatted(Formatting.RED)));
            return 0;
        }

        if (!starter.isPaused()) {
            ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                    .append(Text.literal("The game start is not paused. Use ").formatted(Formatting.RED))
                    .append(Text.literal("/pause").formatted(Formatting.YELLOW)
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pause"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to pause")))))
                    .append(Text.literal(" to pause.").formatted(Formatting.RED)));
            return 0;
        }

        starter.setPaused(false);

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("Resumed the game start").formatted(Formatting.GRAY)));

        return 1;
    }
}

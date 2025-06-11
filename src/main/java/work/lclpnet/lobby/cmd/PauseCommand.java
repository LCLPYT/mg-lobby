package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.cmd.type.KibuCommand;
import work.lclpnet.lobby.game.start.GameStarter;

public class PauseCommand implements KibuCommand {

    private final GameStarter starter;

    public PauseCommand(GameStarter starter) {
        this.starter = starter;
    }

    @Override
    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("pause")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(this::pause);
    }

    private int pause(CommandContext<ServerCommandSource> ctx) {
        if (starter == null) {
            ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                    .append(Text.literal("There is no game starting at the moment").formatted(Formatting.RED)));
            return 0;
        }

        if (starter.isPaused()) {
            ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                    .append(Text.literal("The game start is already paused. Use ").formatted(Formatting.RED))
                    .append(Text.literal("/resume").formatted(Formatting.YELLOW)
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent.RunCommand("/resume"))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to resume")))))
                    .append(Text.literal(" to unpause.").formatted(Formatting.RED)));
            return 0;
        }

        starter.setPaused(true);

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("Paused the game start").formatted(Formatting.GRAY)));

        return 1;
    }
}

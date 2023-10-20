package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.kibu.plugin.cmd.KibuCommand;
import work.lclpnet.lobby.game.GameFinisher;

public class EndCommand implements KibuCommand {

    private final GameFinisher finisher;

    public EndCommand(GameFinisher finisher) {
        this.finisher = finisher;
    }

    @Override
    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("end")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(this::end);
    }

    private int end(CommandContext<ServerCommandSource> ctx) {
        finisher.finishGame(GameFinisher.Reason.COMMAND);

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("The current game has been ended.").formatted(Formatting.GRAY)));

        return 1;
    }
}

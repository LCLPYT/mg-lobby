package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.cmd.type.KibuCommand;
import work.lclpnet.lobby.game.api.GameFinisher;

public class EndCommand implements KibuCommand {

    private final GameFinisher finisher;

    public EndCommand(GameFinisher finisher) {
        this.finisher = finisher;
    }

    @Override
    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("end")
                .requires(s -> s.hasPermission(2))
                .executes(this::end);
    }

    private int end(CommandContext<CommandSourceStack> ctx) {
        finisher.finishGame(GameFinisher.Reason.COMMAND);

        ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal("The current game has been ended.").withStyle(ChatFormatting.GRAY)));

        return 1;
    }
}

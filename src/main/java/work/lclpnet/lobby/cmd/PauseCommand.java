package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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

    private LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("pause")
                .requires(s -> s.hasPermission(2))
                .executes(this::pause);
    }

    private int pause(CommandContext<CommandSourceStack> ctx) {
        if (starter == null) {
            ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                    .append(Component.literal("There is no game starting at the moment").withStyle(ChatFormatting.RED)));
            return 0;
        }

        if (starter.isPaused()) {
            ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                    .append(Component.literal("The game start is already paused. Use ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("/resume").withStyle(ChatFormatting.YELLOW)
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent.RunCommand("/resume"))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to resume")))))
                    .append(Component.literal(" to unpause.").withStyle(ChatFormatting.RED)));
            return 0;
        }

        starter.setPaused(true);

        ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal("Paused the game start").withStyle(ChatFormatting.GRAY)));

        return 1;
    }
}

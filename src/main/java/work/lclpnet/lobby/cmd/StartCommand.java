package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.cmd.type.KibuCommand;
import work.lclpnet.lobby.game.api.option.GameOptions;
import work.lclpnet.lobby.game.start.GameStarter;

public class StartCommand implements KibuCommand {

    private final GameStarter starter;
    private final GameOptions options;

    public StartCommand(GameStarter starter, GameOptions options) {
        this.starter = starter;
        this.options = options;
    }

    @Override
    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("start")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        if (starter == null) {
            ctx.getSource().sendFailure(Component.literal("There is no game to start at the moment."));
            return -1;
        }

        if (starter.isStarted()) {
            ctx.getSource().sendFailure(Component.literal("There game was already started."));
            return -1;
        }

        ctx.getSource().sendSystemMessage(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal("Started the game.").withStyle(ChatFormatting.GRAY)));

        starter.finish(options);

        return 0;
    }
}

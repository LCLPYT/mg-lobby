package work.lclpnet.lobby.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.lobby.game.start.GameStarter;

import java.util.function.Supplier;

public class StartCommand {

    private final Supplier<GameStarter> starterSupplier;

    public StartCommand(Supplier<GameStarter> starterSupplier) {
        this.starterSupplier = starterSupplier;
    }

    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("start")
                .requires((s) -> s.hasPermissionLevel(2))
                .executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        GameStarter starter = starterSupplier.get();

        if (starter == null) {
            ctx.getSource().sendError(Text.literal("There is no game to start at the moment."));
            return -1;
        }

        if (starter.isStarted()) {
            ctx.getSource().sendError(Text.literal("There game was already started."));
            return -1;
        }

        ctx.getSource().sendMessage(Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal("Started the game.").formatted(Formatting.GRAY)));

        starter.start();

        return 0;
    }
}

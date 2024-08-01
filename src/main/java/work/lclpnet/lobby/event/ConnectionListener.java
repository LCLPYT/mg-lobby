package work.lclpnet.lobby.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;

public class ConnectionListener implements HookListenerModule {

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerConnectionHooks.JOIN_MESSAGE, this::onJoinMessage);
        registrar.registerHook(PlayerConnectionHooks.QUIT_MESSAGE, this::onQuitMessage);
    }

    private Text onJoinMessage(ServerPlayerEntity player, Text joinMessage) {
        return Text.literal("Join> ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal(player.getNameForScoreboard()).formatted(Formatting.GRAY));
    }

    private Text onQuitMessage(ServerPlayerEntity player, Text joinMessage) {
        return Text.literal("Quit> ").formatted(Formatting.DARK_GRAY)
                .append(Text.literal(player.getNameForScoreboard()).formatted(Formatting.GRAY));
    }
}

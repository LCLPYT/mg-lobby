package work.lclpnet.lobby.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;

public class ConnectionListener implements HookListenerModule {

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerConnectionHooks.JOIN_MESSAGE, this::onJoinMessage);
        registrar.registerHook(PlayerConnectionHooks.QUIT_MESSAGE, this::onQuitMessage);
    }

    private Component onJoinMessage(ServerPlayer player, Component joinMessage) {
        return Component.literal("Join> ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.GRAY));
    }

    private Component onQuitMessage(ServerPlayer player, Component joinMessage) {
        return Component.literal("Quit> ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.GRAY));
    }
}

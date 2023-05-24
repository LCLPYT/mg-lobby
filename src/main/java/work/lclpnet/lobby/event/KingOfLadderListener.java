package work.lclpnet.lobby.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.hook.ServerPlayConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.decor.KingOfLadder;

public class KingOfLadderListener implements HookListenerModule {

    private final KingOfLadder kingOfLadder;

    public KingOfLadderListener(KingOfLadder kingOfLadder) {
        this.kingOfLadder = kingOfLadder;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onPlayerMove);
        registrar.registerHook(ServerPlayConnectionHooks.DISCONNECT, this::onDisconnect);
    }

    private void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        kingOfLadder.playerQuit(handler.getPlayer());
    }

    private boolean onPlayerMove(ServerPlayerEntity player, PositionRotation from, PositionRotation to) {
        kingOfLadder.update(player, to);
        return false;
    }
}

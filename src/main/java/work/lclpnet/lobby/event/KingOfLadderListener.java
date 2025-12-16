package work.lclpnet.lobby.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.ServerPlayConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.lobby.decor.KingOfLadder;

import javax.inject.Inject;

public class KingOfLadderListener implements HookListenerModule {

    private final KingOfLadder kingOfLadder;

    @Inject
    public KingOfLadderListener(KingOfLadder kingOfLadder) {
        this.kingOfLadder = kingOfLadder;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onPlayerMove);
        registrar.registerHook(ServerPlayConnectionHooks.DISCONNECT, this::onDisconnect);
    }

    private void onDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        kingOfLadder.playerQuit(handler.getPlayer());
    }

    private boolean onPlayerMove(ServerPlayer player, PositionRotation from, PositionRotation to) {
        kingOfLadder.update(player, to);
        return false;
    }
}

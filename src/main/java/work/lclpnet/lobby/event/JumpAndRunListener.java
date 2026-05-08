package work.lclpnet.lobby.event;

import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.lobby.decor.jnr.JumpAndRun;

public class JumpAndRunListener implements HookListenerModule {

    private final JumpAndRun jumpAndRun;

    public JumpAndRunListener(JumpAndRun jumpAndRun) {
        this.jumpAndRun = jumpAndRun;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onPlayerMove);
    }

    private boolean onPlayerMove(ServerPlayer player, PositionRotation from, PositionRotation to) {
        jumpAndRun.update(player, to);
        return false;
    }
}

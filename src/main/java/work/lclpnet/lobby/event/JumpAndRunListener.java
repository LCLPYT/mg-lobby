package work.lclpnet.lobby.event;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.decor.jnr.JumpAndRun;

import javax.inject.Inject;

public class JumpAndRunListener implements HookListenerModule {

    private final JumpAndRun jumpAndRun;

    @Inject
    public JumpAndRunListener(JumpAndRun jumpAndRun) {
        this.jumpAndRun = jumpAndRun;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onPlayerMove);
    }

    private boolean onPlayerMove(ServerPlayerEntity player, PositionRotation from, PositionRotation to) {
        jumpAndRun.update(player, to);
        return false;
    }
}

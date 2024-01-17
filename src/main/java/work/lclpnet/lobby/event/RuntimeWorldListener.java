package work.lclpnet.lobby.event;

import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.map.hook.MapStateCallback;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import xyz.nucleoid.fantasy.RuntimeWorld;

public class RuntimeWorldListener implements HookListenerModule {

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(MapStateCallback.HOOK, this::getRuntimeMapState);
    }

    @Nullable
    private MapState getRuntimeMapState(ServerWorld world, String id) {
        if (!(world instanceof RuntimeWorld runtimeWorld)) return null;

        return runtimeWorld.getPersistentStateManager().get(MapState.getPersistentStateType(), id);
    }
}

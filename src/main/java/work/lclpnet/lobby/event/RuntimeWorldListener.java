package work.lclpnet.lobby.event;

import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.map.hook.MapStateCallback;
import xyz.nucleoid.fantasy.RuntimeWorld;

public class RuntimeWorldListener implements HookListenerModule {

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(MapStateCallback.HOOK, this::getRuntimeMapState);
    }

    @Nullable
    private MapState getRuntimeMapState(ServerWorld world, MapIdComponent id) {
        if (!(world instanceof RuntimeWorld runtimeWorld)) return null;

        return runtimeWorld.getPersistentStateManager().get(MapState.getPersistentStateType(), id.asString());
    }
}

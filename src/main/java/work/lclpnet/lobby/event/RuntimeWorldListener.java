package work.lclpnet.lobby.event;

import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.server.level.ServerLevel;
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
    private MapItemSavedData getRuntimeMapState(ServerLevel world, MapId id) {
        if (!(world instanceof RuntimeWorld runtimeWorld)) return null;

        return runtimeWorld.getDataStorage().get(MapItemSavedData.type(id));
    }
}

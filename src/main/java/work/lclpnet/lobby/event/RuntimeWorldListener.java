package work.lclpnet.lobby.event;

import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.map.hook.MapStateCallback;
import xyz.nucleoid.fantasy.RuntimeLevel;

public class RuntimeWorldListener implements HookListenerModule {

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(MapStateCallback.HOOK, this::getRuntimeMapState);
    }

    @Nullable
    private MapItemSavedData getRuntimeMapState(ServerLevel level, MapId id) {
        if (!(level instanceof RuntimeLevel runtimeLevel)) return null;

        return runtimeLevel.getDataStorage().get(MapItemSavedData.type(id));
    }
}

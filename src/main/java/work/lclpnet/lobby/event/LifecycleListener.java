package work.lclpnet.lobby.event;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.hook.ServerLifecycleHooks;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.api.ServerAccess;

public class LifecycleListener implements HookListenerModule, ServerAccess {

    private MinecraftServer server = null;

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(ServerLifecycleHooks.SERVER_STARTING, server -> this.server = server);
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }
}

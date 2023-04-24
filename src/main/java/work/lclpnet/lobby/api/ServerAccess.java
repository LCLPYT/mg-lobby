package work.lclpnet.lobby.api;

import net.minecraft.server.MinecraftServer;

public interface ServerAccess {  // TODO move to kibu-plugins as api

    MinecraftServer getServer();
}

package work.lclpnet.lobby.util;

import net.minecraft.server.network.ServerPlayerEntity;

public interface Interactable {

    void onInteract(ServerPlayerEntity player);
}

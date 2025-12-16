package work.lclpnet.lobby.util;

import net.minecraft.server.level.ServerPlayer;

public interface Interactable {

    void onInteract(ServerPlayer player);
}

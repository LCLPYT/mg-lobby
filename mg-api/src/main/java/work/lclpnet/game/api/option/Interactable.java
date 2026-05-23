package work.lclpnet.game.api.option;

import net.minecraft.server.level.ServerPlayer;

public interface Interactable {

    void onInteract(ServerPlayer player);
}

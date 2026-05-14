package work.lclpnet.game.api.start;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import work.lclpnet.game.api.GameContext;

import java.util.function.Function;

public interface GameStatusManager {

    GameContext getContext();

    void setCannotStartMessage(Function<ServerPlayer, Component> messageFunction);

    void setCannotStartBossBarValue(Object value);
}

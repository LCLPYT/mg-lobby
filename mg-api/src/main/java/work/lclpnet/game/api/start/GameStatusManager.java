package work.lclpnet.game.api.start;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.game.api.GameContext;

import java.util.function.Function;

public interface GameStatusManager {

    GameContext getContext();

    void setCannotStartMessage(Function<ServerPlayer, Component> messageFunction);

    void setCannotStartBossBarValue(Object value);
}

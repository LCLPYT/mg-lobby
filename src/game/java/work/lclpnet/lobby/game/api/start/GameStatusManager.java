package work.lclpnet.lobby.game.api.start;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import work.lclpnet.lobby.game.api.GameContext;

import java.util.function.Function;

public interface GameStatusManager {

    GameContext getContext();

    void setCannotStartMessage(Function<ServerPlayerEntity, Text> messageFunction);

    void setCannotStartBossBarValue(Object value);
}

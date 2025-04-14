package work.lclpnet.lobby.game.api.option;

import work.lclpnet.lobby.game.api.GameContext;

public interface GameOptionConfig {

    GameContext getContext();

    <T> void registerVoting(String name, OptionVoting<T> voting);
}

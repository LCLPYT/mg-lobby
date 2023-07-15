package work.lclpnet.lobby.di;

import dagger.Subcomponent;
import work.lclpnet.lobby.decor.GeyserManager;
import work.lclpnet.lobby.decor.KingOfLadder;
import work.lclpnet.lobby.decor.maze.LobbyMazeCreator;
import work.lclpnet.lobby.decor.seat.SeatHandler;
import work.lclpnet.lobby.decor.ttt.TicTacToeManager;
import work.lclpnet.lobby.event.JumpAndRunListener;
import work.lclpnet.lobby.event.KingOfLadderListener;
import work.lclpnet.lobby.event.LobbyListener;
import work.lclpnet.lobby.event.TicTacToeListener;
import work.lclpnet.lobby.game.start.DefaultGameStarter;
import work.lclpnet.lobby.util.ResetWorldModifier;

@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    LobbyListener lobbyListener();

    ResetWorldModifier resetWorldModifier();

    LobbyMazeCreator mazeGenerator();

    KingOfLadder kingOfLadder();

    KingOfLadderListener kingOfLadderListener();

    GeyserManager geyserManager();

    JumpAndRunListener jumpAndRunListener();

    SeatHandler seatHandler();

    TicTacToeManager ticTacToeManager();

    TicTacToeListener ticTacToeListener();

    DefaultGameStarter.Factory defaultGameStarter();

    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule module);

        ActivityComponent build();
    }
}

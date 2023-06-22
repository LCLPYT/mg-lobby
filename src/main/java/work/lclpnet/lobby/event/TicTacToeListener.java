package work.lclpnet.lobby.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.decor.seat.PlayerSeatCallback;
import work.lclpnet.lobby.decor.ttt.TicTacToeManager;

public class TicTacToeListener implements HookListenerModule {

    private final TicTacToeManager ticTacToeManager;

    public TicTacToeListener(TicTacToeManager ticTacToeManager) {
        this.ticTacToeManager = ticTacToeManager;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerSeatCallback.BEFORE_SIT, this::onSeat);
        registrar.registerHook(PlayerSeatCallback.AFTER_SIT, ticTacToeManager::startPlaying);
        registrar.registerHook(PlayerSeatCallback.AFTER_GET_UP, ticTacToeManager::stopPlaying);
    }

    private boolean onSeat(ServerPlayerEntity player, BlockPos pos) {
        // disallow seating when the player is currently playing tic-tac-toe
        return ticTacToeManager.isPlaying(player);
    }
}

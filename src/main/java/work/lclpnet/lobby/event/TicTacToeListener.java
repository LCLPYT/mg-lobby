package work.lclpnet.lobby.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import work.lclpnet.kibu.hook.HookListenerModule;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
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
        registrar.registerHook(PlayerInteractionHooks.USE_BLOCK, this::onUseBlock);
        registrar.registerHook(PlayerInteractionHooks.ATTACK_BLOCK, this::onAttackBlock);
    }

    private boolean onSeat(ServerPlayer player, BlockPos pos) {
        // disallow seating when the player is currently playing tic-tac-toe
        return ticTacToeManager.isPlaying(player);
    }

    private InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayer serverPlayer) || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (ticTacToeManager.tryPlay(serverPlayer, hitResult)) {
            return InteractionResult.SUCCESS;
        }

        if (ticTacToeManager.isTableCenter(hitResult.getBlockPos())) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    private InteractionResult onAttackBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (!(player instanceof ServerPlayer serverPlayer) || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        HitResult result = player.pick(5d, 0f, false);
        if (result.getType() != HitResult.Type.BLOCK || !(result instanceof BlockHitResult hitResult)) return InteractionResult.PASS;

        if (ticTacToeManager.tryPlay(serverPlayer, hitResult)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}

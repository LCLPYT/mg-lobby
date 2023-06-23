package work.lclpnet.lobby.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
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
        registrar.registerHook(PlayerInteractionHooks.USE_BLOCK, this::onUseBlock);
        registrar.registerHook(PlayerInteractionHooks.ATTACK_BLOCK, this::onAttackBlock);
    }

    private boolean onSeat(ServerPlayerEntity player, BlockPos pos) {
        // disallow seating when the player is currently playing tic-tac-toe
        return ticTacToeManager.isPlaying(player);
    }

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || hand != Hand.MAIN_HAND) return ActionResult.PASS;

        if (ticTacToeManager.tryPlay(serverPlayer, hitResult)) {
            return ActionResult.SUCCESS;
        }

        if (ticTacToeManager.isTableCenter(hitResult.getBlockPos())) {
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || hand != Hand.MAIN_HAND) return ActionResult.PASS;

        HitResult result = player.raycast(5d, 0f, false);
        if (result.getType() != HitResult.Type.BLOCK || !(result instanceof BlockHitResult hitResult)) return ActionResult.PASS;

        if (ticTacToeManager.tryPlay(serverPlayer, hitResult)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}

package work.lclpnet.lobby.decor.seat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookFactory;

public class PlayerSeatCallback {

    public static final Hook<BeforeSit> BEFORE_SIT = HookFactory.createArrayBacked(BeforeSit.class, callbacks -> (player, pos) -> {
        for (var callback : callbacks) {
            if (callback.onSeat(player, pos)) {
                return true;
            }
        }

        return false;
    });

    public static final Hook<AfterSit> AFTER_SIT = HookFactory.createArrayBacked(AfterSit.class, callbacks -> (player, pos) -> {
        for (var callback : callbacks) {
            callback.onSeated(player, pos);
        }
    });

    public static final Hook<AfterGetUp> AFTER_GET_UP = HookFactory.createArrayBacked(AfterGetUp.class, callbacks -> (player) -> {
        for (var callback : callbacks) {
            callback.onGottenUp(player);
        }
    });

    public interface BeforeSit {
        boolean onSeat(ServerPlayerEntity player, BlockPos pos);
    }

    public interface AfterSit {
        void onSeated(ServerPlayerEntity player, BlockPos pos);
    }

    public interface AfterGetUp {
        void onGottenUp(ServerPlayerEntity player);
    }
}

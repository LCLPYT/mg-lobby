package work.lclpnet.lobby.decor.seat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.hook.HookFactory;

public class PlayerSeatCallback {

    public static final Hook<Before> BEFORE = HookFactory.createArrayBacked(Before.class, callbacks -> (player, pos) -> {
        for (var callback : callbacks) {
            if (callback.onSeat(player, pos)) {
                return true;
            }
        }

        return false;
    });

    public static final Hook<After> AFTER = HookFactory.createArrayBacked(After.class, callbacks -> (player, pos) -> {
        for (var callback : callbacks) {
            callback.onSeated(player, pos);
        }
    });

    public interface Before {
        boolean onSeat(ServerPlayerEntity player, BlockPos pos);
    }

    public interface After {
        void onSeated(ServerPlayerEntity player, BlockPos pos);
    }
}

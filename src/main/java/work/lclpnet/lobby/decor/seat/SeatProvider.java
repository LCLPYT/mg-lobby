package work.lclpnet.lobby.decor.seat;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface SeatProvider {

    @Nullable
    Entity getSeat(Level world, BlockPos pos);
}

package work.lclpnet.lobby.decor.seat;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface SeatProvider {
    @Nullable
    Entity getSeat(World world, BlockPos pos);
}

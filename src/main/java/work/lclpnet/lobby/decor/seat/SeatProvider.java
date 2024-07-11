package work.lclpnet.lobby.decor.seat;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface SeatProvider {

    @Nullable
    Entity getSeat(World world, BlockPos pos);
}

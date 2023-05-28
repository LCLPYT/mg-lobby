package work.lclpnet.lobby.decor;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GeyserManager {

    private final Geyser[] geysers;

    public GeyserManager(ServerWorld world, List<BlockPos> positions) {
        this.geysers = positions.stream()
                .map(pos -> new Geyser(world, pos))
                .toArray(Geyser[]::new);
    }

    public void tick() {
        for (Geyser geyser : geysers) {
            geyser.tick();
        }
    }
}

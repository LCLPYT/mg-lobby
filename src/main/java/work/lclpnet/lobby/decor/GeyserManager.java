package work.lclpnet.lobby.decor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import java.util.List;

public class GeyserManager {

    private final Geyser[] geysers;

    public GeyserManager(ServerLevel world, LobbyWorldConfig config) {
        this(world, config.geysers);
    }

    public GeyserManager(ServerLevel world, List<BlockPos> positions) {
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

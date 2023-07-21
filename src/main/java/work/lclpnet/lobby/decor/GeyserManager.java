package work.lclpnet.lobby.decor;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.di.ActivityScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@ActivityScope
public class GeyserManager {

    private final Geyser[] geysers;

    @Inject
    public GeyserManager(@Named("lobbyWorld") ServerWorld world, LobbyWorldConfig config) {
        this(world, config.geysers);
    }

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

package work.lclpnet.lobby.decor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.di.ActivityScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@ActivityScope
public class GeyserManager {

    private final Geyser[] geysers;

    @Inject
    public GeyserManager(@Named("lobbyWorld") ServerLevel world, LobbyWorldConfig config) {
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

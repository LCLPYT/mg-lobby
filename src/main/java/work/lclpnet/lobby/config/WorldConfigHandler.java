package work.lclpnet.lobby.config;

import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.config.json.ConfigSerializer;
import work.lclpnet.config.json.JsonConfig;

import java.nio.file.Path;

public class WorldConfigHandler<T extends JsonConfig> extends ConfigHandler<T> {

    public WorldConfigHandler(ServerWorld world, Path relativePath, ConfigSerializer<T> serializer, Logger logger) {
        super(getSavePath(world).resolve(relativePath), serializer, logger);
    }

    @NotNull
    private static Path getSavePath(ServerWorld world) {
        String levelName = world.getChunkManager().chunkLoadingManager.getSaveDir();
        return world.getServer().getRunDirectory().resolve(levelName);
    }
}

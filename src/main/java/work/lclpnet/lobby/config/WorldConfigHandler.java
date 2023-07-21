package work.lclpnet.lobby.config;

import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.config.json.ConfigSerializer;
import work.lclpnet.config.json.JsonConfig;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class WorldConfigHandler<T extends JsonConfig> extends ConfigHandler<T> {

    public WorldConfigHandler(ServerWorld world, Path relativePath, ConfigSerializer<T> serializer, Logger logger) {
        super(getSavePath(world).resolve(relativePath), serializer, logger);
    }

    @Nonnull
    private static Path getSavePath(ServerWorld world) {
        String levelName = world.getChunkManager().threadedAnvilChunkStorage.getSaveDir();
        return world.getServer().getRunDirectory().toPath().resolve(levelName);
    }
}

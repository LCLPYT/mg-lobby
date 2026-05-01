package work.lclpnet.lobby.config;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.config.json.ConfigSerializer;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.lobby.mixin.MinecraftServerAccessor;

import java.nio.file.Path;

public class WorldConfigHandler<T extends JsonConfig> extends ConfigHandler<T> {

    public WorldConfigHandler(ServerLevel world, Path relativePath, ConfigSerializer<T> serializer, Logger logger) {
        super(getLevelSavePath(world).resolve(relativePath), serializer, logger);
    }

    @NotNull
    private static Path getLevelSavePath(ServerLevel world) {
        return ((MinecraftServerAccessor) world.getServer()).getStorageSource().getLevelDirectory().path();
    }
}

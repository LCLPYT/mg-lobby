package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.lobby.LobbyPlugin;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class LobbyActivity implements Activity {

    private final PluginContext pluginContext;

    public LobbyActivity(PluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public void startActivity(PluginContext context) {
        MinecraftServer server = pluginContext.getEnvironment().getServer();
        var players = PlayerLookup.all(server);

        Fantasy fantasy = Fantasy.get(server);

        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(server.getOverworld().getDimensionEntry())
                .setDifficulty(Difficulty.HARD)
                .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
                .setSeed(1234L);

        RuntimeWorldHandle handle = fantasy.getOrOpenPersistentWorld(LobbyPlugin.identifier("test"), config);
        ServerWorld world = handle.asWorld();

        for (ServerPlayerEntity player : players) {
            player.teleport(world, 0, 100, 0, 0f, 0f);
        }
    }

    @Override
    public void endActivity(PluginContext context) {

    }
}

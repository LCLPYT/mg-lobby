package work.lclpnet.lobby.dev;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameEnvironment;
import work.lclpnet.lobby.game.GameProvider;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;

public class TestGameProvider implements GameProvider {

    @Override
    public Game provideGame() {
        return new Game() {
            @Override
            public GameConfig getConfig() {
                return new MinecraftGameConfig("test", "Test Game", new ItemStack(Items.STRUCTURE_VOID));
            }

            @Override
            public boolean canStart(GameEnvironment environment) {
                MinecraftServer server = environment.getServer();
                return !PlayerLookup.all(server).isEmpty();
            }

            @Override
            public void start(GameEnvironment environment) {
                System.out.println("The test game was started!");
            }
        };
    }
}

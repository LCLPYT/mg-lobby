package work.lclpnet.lobby.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameEnvironment;
import work.lclpnet.lobby.game.GameInstance;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;
import work.lclpnet.mplugins.ext.PluginUnloader;

public class TestGame implements Game {

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("test", "Test Game", new ItemStack(Items.STRUCTURE_VOID));
    }

    @Override
    public PluginUnloader getOwner() {
        // implementations should return their owning plugin instance, e.g. via a static getInstance() method
        return LobbyPlugin.getInstance();
    }

    @Override
    public GameInstance createInstance(GameEnvironment environment) {
        return new TestGameInstance(environment);
    }
}

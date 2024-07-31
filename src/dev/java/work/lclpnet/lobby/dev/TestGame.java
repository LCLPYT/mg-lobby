package work.lclpnet.lobby.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;

public class TestGame implements Game {

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("test", new ItemStack(Items.STRUCTURE_VOID));
    }

    @Override
    public GameInstance createInstance(GameEnvironment environment) {
        return new TestGameInstance(environment);
    }

    // optional, use this if the game requires data packs that must be loaded at bootstrap (e.g. world generators, biomes, other static registry data)
    @Override
    public GameDataPacks getBootstrapDataPacks() {
        return new TestGameDataPacks();
    }
}

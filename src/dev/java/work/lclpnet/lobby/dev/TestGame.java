package work.lclpnet.lobby.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;

public class TestGame implements Game {

    public static final String MOD_ID = "mg-lobby-dev";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("test", new ItemStack(Items.STRUCTURE_VOID));
    }

    @Override
    public GameFactory createFactory() {
        // will be called each time this game is selected to be played
        return new TestGameFactory();
    }

    // optional, use this if the game requires data packs that must be loaded at bootstrap (e.g. world generators, biomes, other static registry data)
    @Override
    public GameDataPacks getBootstrapDataPacks() {
        return new TestGameDataPacks();
    }
}

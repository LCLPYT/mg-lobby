package work.lclpnet.lobby.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import work.lclpnet.lobby.game.Game;
import work.lclpnet.lobby.game.GameEnvironment;
import work.lclpnet.lobby.game.GameInstance;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;

public class TestGame implements Game {

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("test", "Test Game", new ItemStack(Items.STRUCTURE_VOID));
    }

    @Override
    public GameInstance createInstance(GameEnvironment environment) {
        return new TestGameInstance(environment);
    }
}

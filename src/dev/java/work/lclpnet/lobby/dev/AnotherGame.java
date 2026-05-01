package work.lclpnet.lobby.dev;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameConfig;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.start.GameScope;
import work.lclpnet.lobby.game.impl.MinecraftGameConfig;
import work.lclpnet.lobby.game.impl.ModGameFactory;

public class AnotherGame implements Game {

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("another", new ItemStackTemplate(Items.RESIN_CLUMP));
    }

    @Override
    public boolean canBePlayed(GameScope scope) {
        return true;
    }

    @Override
    public GameFactory createFactory() {
        return new ModGameFactory(TestGame.MOD_ID, TestGame.logger, AnotherGameInstance::new);
    }
}

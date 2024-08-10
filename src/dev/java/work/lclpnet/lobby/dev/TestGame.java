package work.lclpnet.lobby.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.lobby.game.api.TranslatedGame;
import work.lclpnet.lobby.game.api.data.GameDataPacks;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;
import work.lclpnet.translations.loader.TranslationLoader;

public class TestGame implements Game, TranslatedGame {

    public static final String MOD_ID = "mg-lobby-dev";
    private static final Logger logger = LoggerFactory.getLogger(MOD_ID);

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

    @Override
    public TranslationLoader getTranslationLoader() {
        // provide a custom translation loader that will be used as translation source by the game framework
        return ModTranslations.assetTranslationLoader(MOD_ID, logger);
    }
}

package work.lclpnet.lobby.dev;

import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.translations.loader.TranslationLoader;

public class TestGameFactory implements GameFactory {

    // this class can have state; createTranslationLoader is called before createInstance

    @Override
    public @Nullable TranslationLoader createTranslationLoader() {
        return ModTranslations.assetTranslationLoader(TestGame.MOD_ID, TestGame.LOGGER);
    }

    @Override
    public GameInstance createInstance(GameEnvironment environment) {
        return new TestGameInstance(environment);
    }
}

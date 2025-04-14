package work.lclpnet.lobby.game.impl;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.game.api.GameEnvironment;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.GameInstance;
import work.lclpnet.translations.loader.TranslationLoader;

import java.util.function.Function;

/**
 * A simple {@link GameFactory} that loads translations of an associated mod.
 */
public class ModGameFactory implements GameFactory {

    private final String modId;
    private final Logger logger;
    private final Function<GameEnvironment, GameInstance> instanceFactory;

    public ModGameFactory(String modId, Logger logger, Function<GameEnvironment, GameInstance> instanceFactory) {
        this.modId = modId;
        this.logger = logger;
        this.instanceFactory = instanceFactory;
    }

    @Override
    public @Nullable TranslationLoader createTranslationLoader() {
        return ModTranslations.assetTranslationLoader(modId, logger);
    }

    @Override
    public GameInstance createInstance(GameEnvironment environment) {
        return instanceFactory.apply(environment);
    }
}

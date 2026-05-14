package work.lclpnet.game.impl;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import work.lclpnet.game.api.GameEnvironment;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.GameInstance;
import work.lclpnet.kibu.translate.util.ModTranslations;
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
    public @NonNull GameInstance createInstance(GameEnvironment environment) {
        return instanceFactory.apply(environment);
    }
}

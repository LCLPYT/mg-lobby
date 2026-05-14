package work.lclpnet.lobby.util;

import org.slf4j.Logger;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.game.GameManager;
import work.lclpnet.game.api.Game;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.MultiTranslationLoader;
import work.lclpnet.translations.loader.TranslationLoader;
import work.lclpnet.translations.model.Language;
import work.lclpnet.translations.model.LanguageCollection;
import work.lclpnet.translations.model.StaticLanguage;
import work.lclpnet.translations.model.StaticLanguageCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LobbyTranslations {

    private final GameManager gameManager;
    private final Logger logger;

    public LobbyTranslations(GameManager gameManager, Logger logger) {
        this.gameManager = gameManager;
        this.logger = logger;
    }

    public ModTranslations.Result load() {
        // load game titles into the lobby translations (needed for game picker)
        var combined = new MultiTranslationLoader();

        for (Game game : gameManager.getGames()) {
            TranslationLoader gameTranslations = game.createFactory().createTranslationLoader();

            // only load the title translation, drop others
            combined.addLoader(new FilterTranslationLoader(gameTranslations, game.getConfig().titleKey()));
        }

        // translations added later will overwrite previous translations, put mg-lobby last
        combined.addLoader(ModTranslations.assetTranslationLoader(LobbyMod.ID, logger));

        return loadFrom(combined);
    }

    private ModTranslations.Result loadFrom(TranslationLoader loader) {
        var translator = new DefaultLanguageTranslator(loader);
        var translations = new Translations(translator);

        var whenLoaded = translator.reload();

        return new ModTranslations.Result(translations, whenLoaded);
    }

    /**
     * Translation loader that loads a single translation from the parent loader.
     * @param allowedKey The key to load.
     */
    private record FilterTranslationLoader(TranslationLoader parent, String allowedKey) implements TranslationLoader {

        @Override
        public CompletableFuture<? extends LanguageCollection> load() {
            return parent.load().thenApply(source -> {
                Map<String, Language> filtered = new HashMap<>();

                for (String languageId : source.keys()) {
                    Language language = source.get(languageId);

                    if (language == null || !language.has(allowedKey)) continue;

                    String translated = language.get(allowedKey);

                    if (translated == null) continue;

                    filtered.put(languageId, new StaticLanguage(Map.of(allowedKey, translated)));
                }

                return new StaticLanguageCollection(filtered);
            });
        }
    }
}

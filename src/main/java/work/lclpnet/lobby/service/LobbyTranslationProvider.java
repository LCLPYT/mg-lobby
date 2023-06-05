package work.lclpnet.lobby.service;

import org.slf4j.Logger;
import work.lclpnet.lobby.LobbyAPI;
import work.lclpnet.translations.loader.TranslationProvider;
import work.lclpnet.translations.loader.language.ClassLoaderLanguageLoader;
import work.lclpnet.translations.loader.language.LanguageLoader;

import java.util.List;

public class LobbyTranslationProvider implements TranslationProvider {

    @Override
    public LanguageLoader create() {
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> resourceDirectories = List.of("lang/");
        Logger logger = LobbyAPI.getInstance().getManager().getLogger();

        return new ClassLoaderLanguageLoader(classLoader, resourceDirectories, logger);
    }
}

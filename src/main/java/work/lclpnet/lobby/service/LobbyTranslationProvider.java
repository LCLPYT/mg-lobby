package work.lclpnet.lobby.service;

import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.translations.loader.TranslationProvider;
import work.lclpnet.translations.loader.language.LanguageLoader;
import work.lclpnet.translations.loader.language.UrlLanguageLoader;

import java.net.URL;
import java.util.List;

public class LobbyTranslationProvider implements TranslationProvider {

    @Override
    public LanguageLoader create() {
        URL[] urls = UrlLanguageLoader.getResourceLocations(this);
        List<String> resourceDirectories = List.of("lang/");

        return new UrlLanguageLoader(urls, resourceDirectories, LobbyPlugin.logger);
    }
}

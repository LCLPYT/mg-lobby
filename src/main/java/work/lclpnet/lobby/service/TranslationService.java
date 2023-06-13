package work.lclpnet.lobby.service;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.lobby.util.RootText;
import work.lclpnet.lobby.util.TextFormatter;
import work.lclpnet.translations.Translator;

public class TranslationService {

    private final Translator translator;
    private final TextFormatter textFormatter = new TextFormatter();

    public TranslationService(Translator translator) {
        this.translator = translator;
    }

    private String getLanguage(ServerPlayerEntity player) {
        // TODO respect configured network language
        return PlayerLanguage.getLanguage(player);
    }

    public String translate(ServerPlayerEntity player, String key, Object... args) {
        String language = getLanguage(player);
        return translator.translate(language, key, args);
    }

    public RootText translateText(ServerPlayerEntity player, String key, Object... args) {
        String raw = translate(player, key);  // do not replace format specifiers

        return textFormatter.formatText(raw, args);
    }
}

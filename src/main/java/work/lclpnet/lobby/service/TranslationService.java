package work.lclpnet.lobby.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.activity.util.BossBarHandler;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.lobby.util.*;
import work.lclpnet.translations.Translator;

import javax.annotation.Nonnull;

public class TranslationService {

    private final Translator translator;
    private final TextFormatter textFormatter = new TextFormatter();

    public TranslationService(Translator translator) {
        this.translator = translator;
    }

    public Translator getTranslator() {
        return translator;
    }

    @Nonnull
    public String getLanguage(ServerPlayerEntity player) {
        // TODO respect configured network language
        return PlayerLanguage.getLanguage(player);
    }

    public String translate(ServerPlayerEntity player, String key) {
        String language = getLanguage(player);
        return translator.translate(language, key);
    }

    public String translate(ServerPlayerEntity player, String key, Object... args) {
        String language = getLanguage(player);
        return translator.translate(language, key, args);
    }

    public RootText translateText(ServerPlayerEntity player, String key, Object... args) {
        return translateText(getLanguage(player), key, args);
    }

    public RootText translateText(String language, String key, Object... args) {
        String raw = translator.translate(language, key);  // do not replace format specifiers

        return textFormatter.formatText(raw, args);
    }

    public TranslatedText translateText(String key, Object... args) {
        return TranslatedText.create(player -> translateText(player, key, args));
    }

    public Partial<TranslatedBossBar, BossBarHandler> translateBossBar(Identifier id, String key, Object... args) {
        return handler -> new TranslatedBossBar(handler, id, this, key, args);
    }
}

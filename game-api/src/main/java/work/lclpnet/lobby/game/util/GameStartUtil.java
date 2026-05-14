package work.lclpnet.lobby.game.util;

import net.minecraft.ChatFormatting;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.TranslatedText;
import work.lclpnet.lobby.game.api.start.GameStatusManager;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class GameStartUtil {

    public static void configureNotEnoughPlayersMessage(GameStatusManager manager, int requiredPlayers) {
        Translations translations = manager.getContext().getTranslations();

        var msg = getNotEnoughPlayersMessage(translations, requiredPlayers);

        manager.setCannotStartMessage(msg::translateFor);
    }

    public static void configureWaitingForPlayersBossBar(GameStatusManager manager) {
        var msg = getWaitingForPlayersMessage(manager.getContext().getTranslations());

        manager.setCannotStartBossBarValue(msg);
    }

    public static TranslatedText getNotEnoughPlayersMessage(Translations translations, int requiredPlayers) {
        return translations.translateText(
                "lobby.game.not_enough_players",
                styled(requiredPlayers, ChatFormatting.YELLOW)
        ).formatted(ChatFormatting.RED);
    }

    public static TranslatedText getWaitingForPlayersMessage(Translations translations) {
        return translations.translateText("lobby.game.waiting_for_players");
    }
}

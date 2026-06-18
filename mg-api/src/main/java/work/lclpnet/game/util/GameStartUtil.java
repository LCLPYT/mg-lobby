package work.lclpnet.game.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import work.lclpnet.game.api.start.GameStartArgs;
import work.lclpnet.game.api.start.GameStatusManager;
import work.lclpnet.game.impl.Voting;
import work.lclpnet.game.impl.VotingHandler;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.TranslatedText;

import static work.lclpnet.kibu.access.entity.ServerPlayerAccess.playSoundToPlayer;
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
                "mg-api.game.not_enough_players",
                styled(requiredPlayers, ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.RED);
    }

    public static TranslatedText getWaitingForPlayersMessage(Translations translations) {
        return translations.translateText("mg-api.game.waiting_for_players");
    }

    /**
     * Set up a voting during game start.
     * An item is given to all players which can be used to open the voting menu.
     * Players joining the lobby later will also receive the item.
     * @param voting The {@link Voting} to set up.
     * @param hooks The {@link HookRegistrar}, usually obtained by a {@link work.lclpnet.activity.ComponentActivity}
     *              that is configured as game-selected-activity using {@link work.lclpnet.game.api.GameFactory#createGameSelectedActivity(GameStartArgs)}.
     * @param slot The preferred hotbar slot for the voting item.
     * @param args The {@link GameStartArgs}, usually obtained as argument of {@link work.lclpnet.game.api.GameFactory#createGameSelectedActivity(GameStartArgs)}.
     * @param reminderTicks The number of ticks before the game start to remind players who haven't voted yet of the voting.
     *                      If zero, players won't be reminded.
     * @return The {@link VotingHandler} for later usage (optional).
     * @param <T> The voting item type.
     */
    public static <T> VotingHandler<T> setupVoting(Voting<T> voting, HookRegistrar hooks, int slot, GameStartArgs args, int reminderTicks) {
        var handler = new VotingHandler<>(voting);

        MinecraftServer server = args.options().getContext().getServer();

        handler.init(hooks);
        handler.giveStackTo(PlayerLookup.all(server), slot);
        handler.giveStackToNewPlayers(hooks, slot);

        if (reminderTicks > 0) {
            args.options().addTimedAction(reminderTicks, () -> {
                for (ServerPlayer player : PlayerLookup.all(server)) {
                    // check if the player has another screen open or has already voted
                    if (player.containerMenu != player.inventoryMenu || voting.hasVoted(player)) continue;

                    voting.open(player);

                    playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 0.5f, 0.5f);
                }
            });
        }

        return handler;
    }
}

package work.lclpnet.lobby.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.ServerMessageHooks;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.RootText;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class WholesomeChatManager {

    private static final List<String> WHOLESOME_MESSAGE_KEYS = List.of(
            "lobby.chat.wholesome.1",
            "lobby.chat.wholesome.2",
            "lobby.chat.wholesome.3",
            "lobby.chat.wholesome.4",
            "lobby.chat.wholesome.5",
            "lobby.chat.wholesome.6",
            "lobby.chat.wholesome.7",
            "lobby.chat.wholesome.8"
    );

    private final MinecraftServer server;
    private final Translations translations;

    public WholesomeChatManager(MinecraftServer server, Translations translations) {
        this.server = server;
        this.translations = translations;
    }

    public void init(HookRegistrar hooks) {
        hooks.registerHook(ServerMessageHooks.ALLOW_CHAT_MESSAGE, (message, player, params) -> {
            if (!isToxic(message)) {
                return true;
            }

            String key = randomWholesomeKey();
            broadcastWholesomeMessage(key, params);
            return false;
        });
    }

    public boolean isToxic(PlayerChatMessage message) {
        String canonical = message.signedBody().content()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");

        return Objects.equals(canonical, "ggez") || canonical.equals("bg");
    }

    private String randomWholesomeKey() {
        int index = ThreadLocalRandom.current().nextInt(WHOLESOME_MESSAGE_KEYS.size());
        return WHOLESOME_MESSAGE_KEYS.get(index);
    }

    private void broadcastWholesomeMessage(String key, ChatType.Bound params) {
        for (var player : PlayerLookup.all(server)) {
            RootText message = translations.translateText(key).translateFor(player);
            player.connection.sendDisguisedChatMessage(message, params);
        }
    }
}

package work.lclpnet.lobby.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.ServerMessageHooks;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.text.RootText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WholesomeChatManager {

    private static final String KEY_PREFIX = "lobby.chat.wholesome.";
    private static final Set<String> TOXIC_MESSAGES = Set.of(
            "ggez", "ggeasy", "bg"
    );

    private final MinecraftServer server;
    private final Translations translations;
    private final List<String> wholesomeMessageKeys;

    public WholesomeChatManager(MinecraftServer server, Translations translations) {
        this.server = server;
        this.translations = translations;
        this.wholesomeMessageKeys = collectWholesomeKeys();
    }

    private List<String> collectWholesomeKeys() {
        var translator = translations.getTranslator();
        var keys = new ArrayList<String>();

        for (int i = 1; translator.hasTranslation("en_us", KEY_PREFIX + i); i++) {
            keys.add(KEY_PREFIX + i);
        }

        return List.copyOf(keys);
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

        return TOXIC_MESSAGES.contains(canonical);
    }

    private String randomWholesomeKey() {
        int index = ThreadLocalRandom.current().nextInt(wholesomeMessageKeys.size());
        return wholesomeMessageKeys.get(index);
    }

    private void broadcastWholesomeMessage(String key, ChatType.Bound params) {
        for (var player : PlayerLookup.all(server)) {
            RootText message = translations.translateText(key).translateFor(player);
            player.connection.sendDisguisedChatMessage(message, params);
        }
    }
}

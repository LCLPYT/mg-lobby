package work.lclpnet.game.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GameConfig {

    int DEFAULT_LOBBY_DURATION_SECONDS = 75;

    /**
     * A unique string identifier of the game.
     * @return The game path.
     */
    @NotNull String identifier();

    /**
     * The translation key of the title of the game.
     * @return The game title.
     */
    default @NotNull String titleKey() {
        return "game.%s.title".formatted(identifier());
    }

    /**
     * An icon for visually displaying the game.
     * @return The game icon.
     */
    @NotNull ItemStack icon();

    /**
     * The time for lobby-like game-hosts to wait for players to join, in seconds.
     * @return The wait duration in seconds.
     */
    default int lobbyDurationSeconds() {
        return DEFAULT_LOBBY_DURATION_SECONDS;
    }
}

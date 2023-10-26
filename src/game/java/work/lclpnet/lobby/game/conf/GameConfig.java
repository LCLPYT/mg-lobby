package work.lclpnet.lobby.game.conf;

import net.minecraft.item.ItemStack;

public interface GameConfig {

    int DEFAULT_START_DURATION_SECONDS = 90;

    /**
     * A unique string identifier of the game.
     * @return The game id.
     */
    String identifier();

    /**
     * The translation key of  title of the game.
     * @return The game title.
     */
    default String titleKey() {
        return "game.%s.title".formatted(identifier());
    }

    /**
     * An icon for visually displaying the game.
     * @return The game icon.
     */
    ItemStack icon();

    default int startDuration() {
        return DEFAULT_START_DURATION_SECONDS;
    }
}

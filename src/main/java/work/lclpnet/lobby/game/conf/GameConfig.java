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
     * The human-readable title of the game.
     * @return The game title.
     */
    String title();

    /**
     * An icon for visually displaying the game.
     * @return The game icon.
     */
    ItemStack icon();

    default int getStartDuration() {
        return DEFAULT_START_DURATION_SECONDS;
    }
}

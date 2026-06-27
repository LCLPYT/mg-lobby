package work.lclpnet.game.api.option.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Post-processes the base icon of an option for a specific viewer before it is placed into a
 * {@link work.lclpnet.game.impl.menu.PaginatedOptionMenu}. Useful to add state-dependent decorations
 * such as an enchantment glint or extra lore (e.g. vote counts).
 *
 * @param <T> The option type.
 */
@FunctionalInterface
public interface IconDecorator<T> {

    /**
     * Decorate the base icon of an option.
     *
     * @param player   The viewer.
     * @param option   The option the icon represents.
     * @param baseIcon The base icon produced by the menu's icon factory.
     * @return The icon to display. May be the modified {@code baseIcon} or a new stack.
     */
    ItemStack decorate(ServerPlayer player, T option, ItemStack baseIcon);

    static <T> IconDecorator<T> none() {
        return (player, option, baseIcon) -> baseIcon;
    }
}

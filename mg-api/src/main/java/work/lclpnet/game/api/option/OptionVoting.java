package work.lclpnet.game.api.option;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public record OptionVoting<T>(
        Function<ServerPlayer, ItemStack> icon,
        Function<ServerPlayer, Component> title,
        Class<T> optionType,
        Collection<T> options,
        BiFunction<ServerPlayer, T, ItemStack> optionIcons
) {
    public BiFunction<ServerPlayer, T, Component> optionName() {
        return optionIcons.andThen(ItemStack::getHoverName);
    }
}

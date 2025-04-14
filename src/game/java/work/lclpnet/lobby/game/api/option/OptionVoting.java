package work.lclpnet.lobby.game.api.option;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public record OptionVoting<T>(
        Function<ServerPlayerEntity, ItemStack> icon,
        Function<ServerPlayerEntity, Text> title,
        Class<T> optionType,
        Collection<T> options,
        BiFunction<ServerPlayerEntity, T, ItemStack> optionIcons
) {
    public BiFunction<ServerPlayerEntity, T, Text> optionName() {
        return optionIcons.andThen(ItemStack::getName);
    }
}

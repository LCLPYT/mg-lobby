package work.lclpnet.game.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import work.lclpnet.game.api.GameConfig;

public record MinecraftGameConfig(
        @NotNull String identifier,
        @NotNull ItemStackTemplate iconTemplate
) implements GameConfig {

    @Override
    public @NonNull ItemStack icon() {
        return iconTemplate.create();
    }
}

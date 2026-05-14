package work.lclpnet.lobby.game.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import work.lclpnet.lobby.game.api.GameConfig;

public record MinecraftGameConfig(
        @NotNull String identifier,
        @NotNull ItemStackTemplate iconTemplate
) implements GameConfig {

    @Override
    public @NonNull ItemStack icon() {
        return iconTemplate.create();
    }
}

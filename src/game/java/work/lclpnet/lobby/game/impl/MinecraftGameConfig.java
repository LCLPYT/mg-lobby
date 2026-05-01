package work.lclpnet.lobby.game.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import work.lclpnet.lobby.game.api.GameConfig;

public record MinecraftGameConfig(String identifier, ItemStackTemplate iconTemplate) implements GameConfig {

    @Override
    public ItemStack icon() {
        return iconTemplate.create();
    }
}

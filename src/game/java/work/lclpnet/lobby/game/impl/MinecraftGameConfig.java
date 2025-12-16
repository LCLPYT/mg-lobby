package work.lclpnet.lobby.game.impl;

import net.minecraft.world.item.ItemStack;
import work.lclpnet.lobby.game.api.GameConfig;

public record MinecraftGameConfig(String identifier, ItemStack icon) implements GameConfig {

}

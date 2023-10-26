package work.lclpnet.lobby.game.conf;

import net.minecraft.item.ItemStack;

public record MinecraftGameConfig(String identifier, ItemStack icon) implements GameConfig {

}

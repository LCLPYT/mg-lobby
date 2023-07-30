package work.lclpnet.lobby.game.conf;

import net.minecraft.item.ItemStack;

public record MinecraftGameConfig(String identifier, String title, ItemStack icon) implements GameConfig {

}

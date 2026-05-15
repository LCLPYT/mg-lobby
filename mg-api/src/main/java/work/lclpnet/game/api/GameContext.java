package work.lclpnet.game.api;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.kibu.translate.Translations;

public interface GameContext {

    @NotNull MinecraftServer getServer();

    @NotNull GameConfig getGameConfig();

    @NotNull Translations getTranslations();
}

package work.lclpnet.activity.component;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface ComponentContext {

    @NotNull Logger getLogger();

    @NotNull MinecraftServer getServer();

    @NotNull ComponentView getComponents();
}

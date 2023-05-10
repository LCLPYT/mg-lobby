package work.lclpnet.lobby.api.component;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public interface ComponentContext {

    Logger getLogger();

    MinecraftServer getServer();

    ComponentView getComponents();
}

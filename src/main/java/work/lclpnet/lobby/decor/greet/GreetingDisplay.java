package work.lclpnet.lobby.decor.greet;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import work.lclpnet.game.util.WorldModifier;
import work.lclpnet.kibu.access.entity.DisplayEntityAccess;
import work.lclpnet.lobby.config.LobbyWorldConfig;

public class GreetingDisplay {

    private final LobbyWorldConfig config;
    private final WorldModifier worldModifier;
    private final ServerLevel world;

    public GreetingDisplay(LobbyWorldConfig config, WorldModifier worldModifier, ServerLevel world) {
        this.config = config;
        this.worldModifier = worldModifier;
        this.world = world;
    }

    public void show() {
        if (config.greetingConfig == null) return;

        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);

        display.setPos(config.greetingConfig.pos());
        DisplayEntityAccess.setText(display, config.greetingConfig.text());

        Quaternionf leftRotation = new Quaternionf().rotationY((float) Math.toRadians(config.greetingConfig.rotationY()));
        Vector3f scale = new Vector3f(config.greetingConfig.scale());

        Transformation transform = new Transformation(null, leftRotation, scale, null);
        DisplayEntityAccess.setTransformation(display, transform);

        DisplayEntityAccess.setBackground(display, 0);

        worldModifier.spawnEntity(display);
    }
}

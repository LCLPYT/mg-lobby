package work.lclpnet.lobby.decor.greet;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import work.lclpnet.kibu.access.entity.DisplayEntityAccess;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.util.WorldModifier;

import javax.inject.Inject;
import javax.inject.Named;

public class GreetingDisplay {

    private final LobbyWorldConfig config;
    private final WorldModifier worldModifier;
    private final ServerWorld world;

    @Inject
    public GreetingDisplay(LobbyWorldConfig config, WorldModifier worldModifier, @Named("lobbyWorld") ServerWorld world) {
        this.config = config;
        this.worldModifier = worldModifier;
        this.world = world;
    }

    public void show() {
        if (config.greetingConfig == null) return;

        DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);

        display.setPosition(config.greetingConfig.pos());
        DisplayEntityAccess.setText(display, config.greetingConfig.text());

        Quaternionf leftRotation = new Quaternionf().rotationY((float) Math.toRadians(config.greetingConfig.rotationY()));
        Vector3f scale = new Vector3f(config.greetingConfig.scale());

        AffineTransformation transform = new AffineTransformation(null, leftRotation, scale, null);
        DisplayEntityAccess.setTransformation(display, transform);

        DisplayEntityAccess.setBackground(display, 0);

        worldModifier.spawnEntity(display);
    }
}

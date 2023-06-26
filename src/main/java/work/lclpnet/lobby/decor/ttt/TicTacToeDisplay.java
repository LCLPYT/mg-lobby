package work.lclpnet.lobby.decor.ttt;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import work.lclpnet.kibu.access.entity.DisplayEntityAccess;
import work.lclpnet.lobby.util.WorldModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicTacToeDisplay {

    private final ServerWorld world;
    private final WorldModifier worldModifier;
    private final Map<TicTacToeTable, List<Entity>> entitiesByTable = new HashMap<>();

    public TicTacToeDisplay(ServerWorld world, WorldModifier worldModifier) {
        this.world = world;
        this.worldModifier = worldModifier;
    }

    public void displayMarker(TicTacToeTable table, int x, int y, BlockState state) {
        var display = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
        DisplayEntityAccess.setBlockState(display, state);

        var transformation = new AffineTransformation(null, null, new Vector3f(0.125f), null);
        DisplayEntityAccess.setTransformation(display, transformation);

        final float pixel = 1 / 16f;
        final float d = 2 * pixel;

        BlockPos pos = table.center();

        display.setPos(
                pos.getX() + pixel + (pixel + d) * (x + 1),
                pos.getY() + 1 - pixel,
                pos.getZ() + pixel + (pixel + d) * (y + 1)
        );

        display.getWorld().playSound(null, display.getX(), display.getY(), display.getZ(),
                SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 0.15f, 1f);

        addEntity(table, display);
        worldModifier.spawnEntity(display);
    }

    private void addEntity(TicTacToeTable table, Entity entity) {
        var list = entitiesByTable.computeIfAbsent(table, key -> new ArrayList<>());
        list.add(entity);
    }

    public void reset(TicTacToeTable table) {
        List<Entity> entities = entitiesByTable.remove(table);
        if (entities == null) return;

        for (Entity entity : entities) {
            entity.discard();
        }
    }
}

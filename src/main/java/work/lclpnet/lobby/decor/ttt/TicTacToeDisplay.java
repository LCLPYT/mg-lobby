package work.lclpnet.lobby.decor.ttt;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
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
    private final Map<TicTacToeTable, DisplayEntity.BlockDisplayEntity> turnIndicators = new HashMap<>();

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

    public void indicateTurn(TicTacToeTable table, int player) {
        DisplayEntity.BlockDisplayEntity indicator = turnIndicators.get(table);
        boolean spawn = false;

        if (indicator == null) {
            indicator = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            spawn = true;

            DisplayEntityAccess.setBlockState(indicator, Blocks.MAGENTA_GLAZED_TERRACOTTA.getDefaultState());
            DisplayEntityAccess.setInterpolationDuration(indicator, 4);

            turnIndicators.put(table, indicator);
        }

        Vec3d direction = table.direction();

        direction = new Vec3d(
                Math.abs(direction.getX()),
                Math.abs(direction.getY()),
                Math.abs(direction.getZ())
        );

        Vec3d normal = direction.crossProduct(new Vec3d(0, 1, 0)).normalize();

        normal = new Vec3d(
                Math.abs(normal.getX()),
                Math.abs(normal.getY()),
                Math.abs(normal.getZ())
        );

        final float pixel = 1 / 16f;
        final float distance = 5 * pixel;

        BlockPos pos = table.center();
        indicator.setPos(
                pos.getX() + 0.5 + normal.getX() * distance - direction.getX() * pixel,
                pos.getY() + 1 - pixel,
                pos.getZ() + 0.5 + normal.getZ() * distance - direction.getZ() * pixel
        );

        Vector3f offset;

        if (player == 0) {
            offset = new Vector3f(
                    (float) (-direction.getX() * pixel * 4 + normal.getX() * pixel * 2),
                    0,
                    (float) (-direction.getZ() * pixel * 4)
            );
        } else {
            offset = new Vector3f(
                    (float) (direction.getX() * pixel * 6),
                    0,
                    (float) (direction.getZ() * pixel * 6 + normal.getZ() * pixel * 2)
            );
        }

        float angle = (float) Math.atan2(direction.getX(), direction.getZ()) + (float) Math.PI * (1 - player);

        AffineTransformation transformation = new AffineTransformation(
                offset,
                new Quaternionf().rotateY(angle),
                new Vector3f(0.125f),
                null
        );

        DisplayEntityAccess.setTransformation(indicator, transformation);
        DisplayEntityAccess.setStartInterpolation(indicator, 0);

        if (spawn) {
            addEntity(table, indicator);
            worldModifier.spawnEntity(indicator);
        }
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

        turnIndicators.remove(table);
    }
}

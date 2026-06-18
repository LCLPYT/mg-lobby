package work.lclpnet.lobby.decor.ttt;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import work.lclpnet.game.util.WorldModifier;
import work.lclpnet.kibu.access.entity.DisplayEntityAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicTacToeDisplay {

    private final ServerLevel world;
    private final WorldModifier worldModifier;
    private final Map<TicTacToeTable, List<Entity>> entitiesByTable = new HashMap<>();
    private final Map<TicTacToeTable, Display.BlockDisplay> turnIndicators = new HashMap<>();

    public TicTacToeDisplay(ServerLevel world, WorldModifier worldModifier) {
        this.world = world;
        this.worldModifier = worldModifier;
    }

    public void displayMarker(TicTacToeTable table, int x, int y, BlockState state) {
        var display = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, world);
        DisplayEntityAccess.setBlockState(display, state);

        var transformation = new Transformation(null, null, new Vector3f(0.125f), null);
        DisplayEntityAccess.setTransformation(display, transformation);

        final float pixel = 1 / 16f;
        final float d = 2 * pixel;

        BlockPos pos = table.center();

        display.setPosRaw(
                pos.getX() + pixel + (pixel + d) * (x + 1),
                pos.getY() + 1 - pixel,
                pos.getZ() + pixel + (pixel + d) * (y + 1)
        );

        display.level().playSound(null, display.getX(), display.getY(), display.getZ(),
                SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.15f, 1f);

        addEntity(table, display);
        worldModifier.spawnEntity(display);
    }

    public void indicateTurn(TicTacToeTable table, int player) {
        Display.BlockDisplay indicator = turnIndicators.get(table);
        boolean spawn = false;

        if (indicator == null) {
            indicator = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, world);
            spawn = true;

            DisplayEntityAccess.setBlockState(indicator, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.MAGENTA).defaultBlockState());
            DisplayEntityAccess.setInterpolationDuration(indicator, 4);

            turnIndicators.put(table, indicator);
        }

        Vec3 direction = table.direction();

        direction = new Vec3(
                Math.abs(direction.x()),
                Math.abs(direction.y()),
                Math.abs(direction.z())
        );

        Vec3 normal = direction.cross(new Vec3(0, 1, 0)).normalize();

        normal = new Vec3(
                Math.abs(normal.x()),
                Math.abs(normal.y()),
                Math.abs(normal.z())
        );

        final float pixel = 1 / 16f;
        final float distance = 5 * pixel;

        BlockPos pos = table.center();
        indicator.setPosRaw(
                pos.getX() + 0.5 + normal.x() * distance - direction.x() * pixel,
                pos.getY() + 1 - pixel,
                pos.getZ() + 0.5 + normal.z() * distance - direction.z() * pixel
        );

        Vector3f offset;

        if (player == 0) {
            offset = new Vector3f(
                    (float) (-direction.x() * pixel * 4 + normal.x() * pixel * 2),
                    0,
                    (float) (-direction.z() * pixel * 4)
            );
        } else {
            offset = new Vector3f(
                    (float) (direction.x() * pixel * 6),
                    0,
                    (float) (direction.z() * pixel * 6 + normal.z() * pixel * 2)
            );
        }

        float angle = (float) Math.atan2(direction.x(), direction.z()) + (float) Math.PI * (1 - player);

        Transformation transformation = new Transformation(
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

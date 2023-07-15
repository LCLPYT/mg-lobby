package work.lclpnet.lobby.util;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.entity.EntityRemovedCallback;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.di.ActivityScope;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ActivityScope
public class ResetWorldModifier implements WorldModifier {

    private final World world;
    private final Map<BlockPos, BlockState> states = new HashMap<>();
    private final IntSet entities = new IntOpenHashSet();
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    @Inject
    public ResetWorldModifier(@Named("lobbyWorld") ServerWorld world, HookRegistrar hookRegistrar) {
        this.world = world;

        hookRegistrar.registerHook(EntityRemovedCallback.HOOK, this::onEntityRemoved);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        synchronized (this) {
            if (!states.containsKey(pos)) {
                BlockState prevState = world.getBlockState(pos);

                if (prevState != state) {
                    states.put(new BlockPos(pos), prevState);
                }
            }
        }

        world.setBlockState(pos, state);
    }

    public void spawnEntity(Entity entity) {
        synchronized (this) {
            entities.add(entity.getId());
        }

        world.spawnEntity(entity);
    }

    public void undo() {
        synchronized (this) {
            enabled.set(false);

            for (var entry : states.entrySet()) {
                world.setBlockState(entry.getKey(), entry.getValue());
            }

            states.clear();

            for (int id : entities) {
                Entity entity = world.getEntityById(id);
                if (entity == null) continue;

                entity.discard();
            }

            entities.clear();

            enabled.set(true);
        }
    }

    private void onEntityRemoved(Entity e, Entity.RemovalReason reason) {
        if (!enabled.get()) return;  // prevent co-modification

        entities.remove(e.getId());
    }
}

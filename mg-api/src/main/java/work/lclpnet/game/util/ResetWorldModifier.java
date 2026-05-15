package work.lclpnet.game.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.EntityRemovedCallback;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResetWorldModifier implements WorldModifier {

    private final ServerLevel world;
    private final Map<BlockPos, BlockState> states = new HashMap<>();
    private final Set<UUID> entities = new HashSet<>();
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    public ResetWorldModifier(ServerLevel world, HookRegistrar hookRegistrar) {
        this.world = world;

        hookRegistrar.registerHook(EntityRemovedCallback.HOOK, this::onEntityRemoved);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state, int flags) {
        synchronized (this) {
            if (!states.containsKey(pos)) {
                BlockState prevState = world.getBlockState(pos);

                if (prevState != state) {
                    states.put(new BlockPos(pos), prevState);
                }
            }
        }

        world.setBlock(pos, state, flags);
    }

    public void spawnEntity(Entity entity) {
        synchronized (this) {
            entities.add(entity.getUUID());
        }

        world.addFreshEntity(entity);
    }

    public void undo() {
        synchronized (this) {
            enabled.set(false);

            for (var entry : states.entrySet()) {
                world.setBlockAndUpdate(entry.getKey(), entry.getValue());
            }

            states.clear();

            for (UUID id : entities) {
                Entity entity = world.getEntity(id);
                if (entity == null) continue;

                entity.discard();
            }

            entities.clear();

            enabled.set(true);
        }
    }

    private void onEntityRemoved(Entity e, Entity.RemovalReason reason) {
        if (!enabled.get()) return;  // prevent co-modification

        entities.remove(e.getUUID());
    }
}

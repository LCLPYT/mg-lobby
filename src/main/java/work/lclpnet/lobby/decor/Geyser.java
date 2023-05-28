package work.lclpnet.lobby.decor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.Random;

public class Geyser {

    private final ServerWorld world;
    private final BlockPos position;
    private final Random random = new Random();
    private int timeout;
    private int eruptionTicks = -1;
    private Box box;

    private int particleDelay;

    public Geyser(ServerWorld world, BlockPos position) {
        this.world = world;
        this.position = position;

        determineNextEruption();
    }

    public void tick() {
        if (timeout != 0) {
            // on timeout
            timeout--;
            return;
        }

        if (eruptionTicks == -1) {
            // should erupt now
            erupt();
        }

        if (eruptionTicks-- != 0) {
            // currently erupting
            erupting();
        } else {
            determineNextEruption();
        }
    }

    private void determineNextEruption() {
        timeout = 200 + random.nextInt(300);  // 10-25 seconds
    }

    private void erupt() {
        eruptionTicks = 120 + random.nextInt(100);  // 6-11 seconds
        particleDelay = 6;
        float eruptionStrength = 0.4f + random.nextFloat() * 0.6f;  // 0.4-1.0 strength

        Vec3d pos = position.toCenterPos();
        box = new Box(
                pos.x - 2, pos.y, pos.z - 2,
                pos.x + 2, pos.y + eruptionStrength * 35, pos.z + 2
        );

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        world.playSound(null, x, y, z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 2f, 0f);
        world.spawnParticles(ParticleTypes.CLOUD, x, pos.getY(), pos.getZ(), 100, 1, 0.1, 1, 0.25);
    }

    private void erupting() {
        Vec3d pos = position.toCenterPos();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        if (particleDelay == 0) {
            world.spawnParticles(ParticleTypes.SNOWFLAKE, x, y + box.getYLength(), z, 40, 0.3, 0.5, 0.3, 0.15);
            world.spawnParticles(ParticleTypes.DOLPHIN, x, y + box.getYLength(), z, 15, 1.5, 0.5, 1.5, 1);
        } else {
            particleDelay--;
        }

        world.playSound(null, x, y, z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.15f, 1 + (random.nextFloat() * 0.4f - 0.2f));
        world.spawnParticles(ParticleTypes.POOF, x, y, z, 15, 0.25, box.getYLength() / 2, 0.25, 0.1);
        world.spawnParticles(ParticleTypes.DOLPHIN, x, y, z, 8, 0.3, box.getYLength() / 2, 0.3, 0.3);
        world.spawnParticles(ParticleTypes.SPIT, x, y, z, 10, 1.5, 0.1, 1.5, 0.15);

        for (Entity entity : findCollidingEntities()) {
            entity.velocityDirty = true;  // TODO not working
            entity.setVelocity(0, 0.4, 0);
        }
    }

    private Collection<? extends Entity> findCollidingEntities() {
        return world.getNonSpectatingEntities(PlayerEntity.class, box);
    }
}

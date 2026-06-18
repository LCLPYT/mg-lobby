package work.lclpnet.lobby.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import work.lclpnet.kibu.access.VelocityModifier;

import java.util.Collection;
import java.util.Random;

public class Geyser {

    private static final double VELOCITY_Y_LIMIT = 1.5;
    private static final double VELOCITY_Y_ACCELERATION = 0.3;
    private static final double VELOCITY_TOP_BOOST = 1.3;  // velocity that is applied at the top end of the geyser
    private static final double PARTICLE_RANGE = 64;

    private final ServerLevel world;
    private final BlockPos position;
    private final Random random = new Random();
    private int timeout;
    private int eruptionTicks = -1;
    private int particleDelay;
    private int soundDelay = 0;
    private AABB box;


    public Geyser(ServerLevel world, BlockPos position) {
        this.world = world;
        this.position = position;

        determineNextEruption();
    }

    public void tick() {
        if (timeout != 0) {
            // on timeout
            if (timeout <= 80) {
                Vec3 pos = Vec3.atCenterOf(position);
                double x = pos.x();
                double y = pos.y();
                double z = pos.z();

                if (soundDelay == 0) {
                    soundDelay = 3;
                    world.playSound(null, x, y, z, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS, SoundSource.AMBIENT, 0.25f, 0.9f + (random.nextFloat() * 0.2f - 0.1f));
                } else {
                    soundDelay--;
                }

                spawnForceParticles(ParticleTypes.SNOWFLAKE, x, y, z, 4, 0.3, 0, 0.3, 0.07);
                world.sendParticles(ParticleTypes.BUBBLE, x, y, z, 1, 0.3, 0, 0.3, 0.07);
            }

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

        Vec3 pos = Vec3.atCenterOf(position);
        box = new AABB(
                pos.x - 2, pos.y, pos.z - 2,
                pos.x + 2, pos.y + eruptionStrength * 35, pos.z + 2
        );

        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        world.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 1.6f, 0f);
        spawnForceParticles(ParticleTypes.CLOUD, x, pos.y(), pos.z(), 100, 1, 0.1, 1, 0.25);
    }

    private void erupting() {
        Vec3 pos = Vec3.atCenterOf(position);
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        double height = box.getYsize();
        double half = height * 0.5;
        double dy = half * 0.5 - 1;

        if (particleDelay == 0) {
            spawnForceParticles(ParticleTypes.SNOWFLAKE, x, y + height, z, 30, 0.3, 0.5, 0.3, 0.15);
            world.sendParticles(ParticleTypes.DOLPHIN, x, y + height, z, 15, 1.5, 0.5, 1.5, 1);
        } else {
            particleDelay--;
        }

        world.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.14f, 1 + (random.nextFloat() * 0.4f - 0.2f));
        spawnForceParticles(ParticleTypes.POOF, x, y + half, z, 15, 0.25, dy, 0.25, 0.1);
        world.sendParticles(ParticleTypes.DOLPHIN, x, y + half, z, 8, 0.3, dy, 0.3, 0.3);
        world.sendParticles(ParticleTypes.SPIT, x, y, z, 10, 1.5, 0.1, 1.5, 0.15);

        for (Entity entity : findCollidingEntities()) {
            Vec3 velocity = entity.getDeltaMovement();

            double vy = velocity.y();
            vy += Math.clamp(VELOCITY_Y_LIMIT - vy, 0, VELOCITY_Y_ACCELERATION);

            // stuck at top end
            if (vy < VELOCITY_Y_ACCELERATION) {
                vy = VELOCITY_TOP_BOOST;
            }

            velocity = new Vec3(
                    velocity.x(),
                    vy,
                    velocity.z()
            );

            VelocityModifier.setVelocity(entity, velocity);
        }
    }

    private Collection<? extends Entity> findCollidingEntities() {
        return world.getEntitiesOfClass(Player.class, box);
    }

    public <T extends ParticleOptions> void spawnForceParticles(T particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        final ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(particle, true, false, x, y, z, (float) dx, (float) dy, (float) dz, (float) speed, count);

        final double rangeSquared = Math.pow(PARTICLE_RANGE, 2);

        for (ServerPlayer player : world.players()) {
            if (player.position().distanceToSqr(x, y, z) > rangeSquared) continue;

            player.connection.send(packet);
        }
    }
}

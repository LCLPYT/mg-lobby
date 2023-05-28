package work.lclpnet.lobby.decor;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import work.lclpnet.kibu.title.Title;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class KingOfLadder {

    private final ServerWorld world;
    private final BlockPos goal;
    private final List<Vec3d> displays;
    private final Collection<UUID> contesting = new HashSet<>();
    private UUID king = null;
    private String kingName = null;

    public KingOfLadder(ServerWorld world, BlockPos goal, List<Vec3d> displays) {
        this.world = world;
        this.goal = goal;
        this.displays = displays;
    }

    public void update(ServerPlayerEntity player, Position position) {
        if (isGoal(position)) {
            contesting.add(player.getUuid());
        } else {
            contesting.remove(player.getUuid());
        }

        updateKing();
    }

    private void updateKing() {
        if (contesting.size() != 1) return;  // there are multiple contesting players

        UUID newKing = contesting.iterator().next();  // get winning contestant
        if (newKing == king) return;

        MinecraftServer server = world.getServer();
        ServerPlayerEntity newKingPlayer = server.getPlayerManager().getPlayer(newKing);

        if (newKingPlayer != null) {
            makeKing(newKingPlayer);
        }
    }

    public void playerQuit(ServerPlayerEntity player) {
        contesting.remove(player.getUuid());
        // player can still be king, regardless if they left
    }

    private boolean isGoal(Position pos) {
        return (int) Math.floor(pos.getX()) == goal.getX() && (int) Math.floor(pos.getY()) == goal.getY() && (int) Math.floor(pos.getZ()) == goal.getZ();
    }

    private void makeKing(ServerPlayerEntity player) {
        UUID formerKing = king;

        king = player.getUuid();
        kingName = player.getEntityName();

        // announce new king
        announceKing();
        notifyKing(player);

        if (formerKing != null && !formerKing.equals(king)) {
            MinecraftServer server = world.getServer();
            ServerPlayerEntity formerKingPlayer = server.getPlayerManager().getPlayer(formerKing);

            if (formerKingPlayer != null) {
                notifyFormerKing(formerKingPlayer);
            }
        }

        updateDisplays();
    }

    private void announceKing() {
        // TODO translate
        MutableText msg = Text.literal("Lobby> ").formatted(Formatting.BLUE)
                .append(Text.literal(kingName).formatted(Formatting.YELLOW))
                .append(Text.literal(" is now the king of the ladder").formatted(Formatting.GREEN));

        MinecraftServer server = world.getServer();
        server.getPlayerManager().broadcast(msg, false);
    }

    private void notifyKing(ServerPlayerEntity player) {
        // TODO translate
        var title = Text.literal("You").formatted(Formatting.GREEN, Formatting.BOLD);
        var subtitle = Text.literal("are now the king of the ladder").formatted(Formatting.AQUA);

        Title.get(player).title(title, subtitle, 5, 15, 5);

        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.NEUTRAL, 2f, 0.0f);
    }

    private void notifyFormerKing(ServerPlayerEntity player) {
        // TODO translate
        var title = Text.literal("You").formatted(Formatting.RED);
        var subtitle = Text.literal("are no longer the king of the ladder").formatted(Formatting.AQUA);

        Title.get(player).title(title, subtitle, 5, 15, 5);

        player.playSound(SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.NEUTRAL, 2f, 0.75f);
    }

    private void updateDisplays() {
        // TODO implement
    }

    public void tick() {
        if (contesting.size() > 1) {
            double x = goal.getX() + 0.5;
            double y = goal.getY();
            double z = goal.getZ() + 0.5;

            world.spawnParticles(ParticleTypes.LAVA, x, y, z, 10, 0.25, 0.25, 0.25, 0.1);
            world.playSound(null, x, y, z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 0.1f, 0f);
        }
    }

    public void reset() {
        king = null;
        kingName = null;
        contesting.clear();
    }
}
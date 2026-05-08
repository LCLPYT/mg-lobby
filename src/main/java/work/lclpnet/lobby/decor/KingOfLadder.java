package work.lclpnet.lobby.decor;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.title.Title;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class KingOfLadder {

    private final ServerLevel world;
    private final BlockPos goal;
    private final List<Vec3> displays;
    private final Translations translations;
    private final Collection<UUID> contesting = new HashSet<>();
    private UUID king = null;
    private String kingName = null;

    public KingOfLadder(ServerLevel world, LobbyWorldConfig config, Translations translations) {
        this(world, config.kingOfLadderGoal, config.kingOfLadderDisplays, translations);
    }

    public KingOfLadder(ServerLevel world, BlockPos goal, List<Vec3> displays, Translations translations) {
        this.world = world;
        this.goal = goal;
        this.displays = displays;
        this.translations = translations;
    }

    public void update(ServerPlayer player, Position position) {
        if (player.level() != world) return;

        if (isGoal(position)) {
            contesting.add(player.getUUID());
        } else {
            contesting.remove(player.getUUID());
        }

        updateKing();
    }

    private void updateKing() {
        if (contesting.size() != 1) return;  // there are multiple contesting players

        UUID newKing = contesting.iterator().next();  // get winning contestant
        if (newKing == king) return;

        MinecraftServer server = world.getServer();
        ServerPlayer newKingPlayer = server.getPlayerList().getPlayer(newKing);

        if (newKingPlayer != null) {
            makeKing(newKingPlayer);
        }
    }

    public void playerQuit(ServerPlayer player) {
        contesting.remove(player.getUUID());
        // player can still be king, regardless if they left
    }

    private boolean isGoal(Position pos) {
        return (int) Math.floor(pos.x()) == goal.getX() && (int) Math.floor(pos.y()) == goal.getY() && (int) Math.floor(pos.z()) == goal.getZ();
    }

    private void makeKing(ServerPlayer player) {
        UUID formerKing = king;

        king = player.getUUID();
        kingName = player.getScoreboardName();

        // announce new king
        announceKing();
        notifyKing(player);

        if (formerKing != null && !formerKing.equals(king)) {
            MinecraftServer server = world.getServer();
            ServerPlayer formerKingPlayer = server.getPlayerList().getPlayer(formerKing);

            if (formerKingPlayer != null) {
                notifyFormerKing(formerKingPlayer);
            }
        }

        updateDisplays();
    }

    private void announceKing() {
        translations.translateText("lobby.king_of_ladder.new_king", styled(kingName, ChatFormatting.YELLOW))
                .formatted(ChatFormatting.GREEN)
                .prefixed(Component.literal("Lobby> ").withStyle(ChatFormatting.BLUE))
                .sendTo(PlayerLookup.level(world));
    }

    private void notifyKing(ServerPlayer player) {
        var title = translations.translateText(player, "lobby.king_of_ladder.you_title").formatted(ChatFormatting.GREEN, ChatFormatting.BOLD);
        var subtitle = translations.translateText(player, "lobby.king_of_ladder.you_subtitle").formatted(ChatFormatting.AQUA);

        Title.get(player).title(title, subtitle, 5, 15, 5);

        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 2f, 0.0f);
    }

    private void notifyFormerKing(ServerPlayer player) {
        var title = translations.translateText(player, "lobby.king_of_ladder.not_you_title").formatted(ChatFormatting.RED);
        var subtitle = translations.translateText(player, "lobby.king_of_ladder.not_you_subtitle").formatted(ChatFormatting.AQUA);

        Title.get(player).title(title, subtitle, 5, 15, 5);

        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.BLAZE_DEATH, SoundSource.NEUTRAL, 2f, 0.75f);
    }

    private void updateDisplays() {
        // TODO implement
    }

    public void tick() {
        if (contesting.size() > 1) {
            double x = goal.getX() + 0.5;
            double y = goal.getY();
            double z = goal.getZ() + 0.5;

            world.sendParticles(ParticleTypes.LAVA, x, y, z, 10, 0.25, 0.25, 0.25, 0.1);
            world.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.1f, 0f);
        }
    }

    public void reset() {
        king = null;
        kingName = null;
        contesting.clear();
    }
}

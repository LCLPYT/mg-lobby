package work.lclpnet.lobby.decor.ttt;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import work.lclpnet.kibu.title.Title;
import work.lclpnet.lobby.service.TranslationService;
import work.lclpnet.lobby.util.WorldModifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TicTacToeManager {

    private final Map<TicTacToeTable, TicTacToeInstance> tables;
    private final TranslationService translations;
    private final Map<UUID, TicTacToeTable> playing = new HashMap<>();
    private final TicTacToeDisplay display;

    public TicTacToeManager(Set<TicTacToeTable> tables, TranslationService translations, ServerWorld world, WorldModifier worldModifier) {
        this.tables = new HashMap<>();

        for (TicTacToeTable table : tables) {
            this.tables.put(table, null);
        }

        this.translations = translations;
        this.display = new TicTacToeDisplay(world, worldModifier);
    }

    public void startPlaying(ServerPlayerEntity player, BlockPos pos) {
        if (isPlaying(player)) return;

        final TicTacToeTable table = getTableAt(pos);

        if (table == null) return;

        final int i = table.playerIndex(pos);

        synchronized (this) {
            if (table.player(i) != null) return;  // table slot is occupied

            table.player(i, player);

            playing.put(player.getUuid(), table);
        }

        update(table, i);
    }

    public void stopPlaying(ServerPlayerEntity player) {
        ServerPlayerEntity opponent;

        synchronized (this) {
            TicTacToeTable table = playing.remove(player.getUuid());
            if (table == null) return;

            int i = table.playerIndex(player);
            if (i == -1) return;

            table.player(i, null);

            if (table.players().isEmpty()) {
                tables.put(table, null);
            }

            opponent = table.opponent(i);
        }

        if (opponent != null) {
            win(opponent);
        }
    }

    private void update(TicTacToeTable table, int causePlayerId) {
        if (!table.full()) {
            for (ServerPlayerEntity player : table.players()) {
                var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
                var subtitle = translations.translateText(player, "lobby.tic_tac_toe.waiting").formatted(Formatting.GRAY, Formatting.ITALIC);

                Title.get(player).title(title, subtitle, 10, 70, 20);
            }

            return;
        }

        tables.put(table, createInstance(table, table.opponent(causePlayerId)));

        for (ServerPlayerEntity player : table.players()) {
            var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
            var subtitle = translations.translateText(player, "lobby.tic_tac_toe.start").formatted(Formatting.GREEN, Formatting.BOLD);

            Title.get(player).title(title, subtitle, 10, 20, 20);
        }
    }

    private TicTacToeInstance createInstance(TicTacToeTable table, ServerPlayerEntity opponent) {
        return new TicTacToeInstance(new TicTacToeGame(), table.playerIndex(opponent));
    }

    private void win(ServerPlayerEntity player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.you_won").formatted(Formatting.GOLD);

        Title.get(player).title(Text.empty(), subtitle, 10, 70, 20);
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1f);
    }

    private void loose(ServerPlayerEntity player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.you_lost").formatted(Formatting.RED);

        Title.get(player).title(Text.empty(), subtitle, 10, 70, 20);
        player.playSound(SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.PLAYERS, 0.6f, 1f);
    }

    public boolean isPlaying(ServerPlayerEntity player) {
        synchronized (this) {
            return playing.containsKey(player.getUuid());
        }
    }

    @Nullable
    private TicTacToeTable getTableAt(BlockPos pos) {
        return tables.keySet().stream()
                .filter(table -> pos.equals(table.left()) || pos.equals(table.right()))
                .findAny().orElse(null);
    }

    public boolean isTableCenter(BlockPos pos) {
        return tables.keySet().stream()
                .anyMatch(table -> pos.equals(table.center()));
    }

    public boolean tryPlay(ServerPlayerEntity player, BlockHitResult hitResult) {
        final TicTacToeTable table;
        final TicTacToeInstance instance;

        synchronized (this) {
            table = playing.get(player.getUuid());
            if (table == null) return false;

            BlockPos pos = hitResult.getBlockPos();
            if (!table.center().equals(pos)) return false;

            instance = tables.get(table);
        }

        if (instance == null) return false;

        final int playerIndex = table.playerIndex(player);

        if (!instance.isPlayersTurn(playerIndex)) return false;

        Vec3i field = getField(hitResult.getPos());
        int x = field.getX();
        int y = field.getY();

        if (!instance.play(playerIndex, x, y)) return false;

        display.displayMarker(table, x, y, Blocks.YELLOW_GLAZED_TERRACOTTA.getDefaultState());

        updateInstance(table, instance);

        return true;
    }

    private void updateInstance(TicTacToeTable table, TicTacToeInstance instance) {
        if (!instance.isGameOver()) return;

        int winnerIndex = instance.getWinner();

        ServerPlayerEntity winner = table.player(winnerIndex);
        win(winner);

        ServerPlayerEntity looser = table.opponent(winnerIndex);
        loose(looser);

        // TODO make combo glowing
    }

    private Vec3i getField(Vec3d vec) {
        final double pixel = 1 / 16d;
        final double width = 3 * pixel;

        final double rx = Math.abs(vec.getX() - (int) vec.getX());
        final double rz = Math.abs(vec.getZ() - (int) vec.getZ());

        int x = (int) ((rx - 0.5 * pixel) / width) - 1;
        int y = (int) ((rz - 0.5 * pixel) / width) - 1;

        return new Vec3i(x, y, 0);
    }

    public void reset() {
        synchronized (this) {
            playing.clear();

            for (TicTacToeTable table : tables.keySet()) {
                table.clear();
                tables.put(table, null);
                display.reset(table);
            }
        }
    }
}

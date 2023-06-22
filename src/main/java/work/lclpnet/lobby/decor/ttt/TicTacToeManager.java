package work.lclpnet.lobby.decor.ttt;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import work.lclpnet.kibu.title.Title;
import work.lclpnet.lobby.service.TranslationService;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TicTacToeManager {

    private final Set<TicTacToeTable> tables;
    private final TranslationService translations;
    private final Map<UUID, TicTacToeTable> playing = new HashMap<>();

    public TicTacToeManager(Set<TicTacToeTable> tables, TranslationService translations) {
        this.tables = tables.stream()
                .filter(TicTacToeManager::isTableValid)
                .collect(Collectors.toUnmodifiableSet());

        this.translations = translations;
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

        update(table);
    }

    public void stopPlaying(ServerPlayerEntity player) {
        ServerPlayerEntity opponent;

        synchronized (this) {
            TicTacToeTable table = playing.remove(player.getUuid());
            if (table == null) return;

            int i = table.playerIndex(player);
            if (i == -1) return;

            table.player(i, null);

            opponent = table.opponent(i);
        }

        if (opponent != null) {
            win(opponent);
        }
    }

    private void update(TicTacToeTable table) {
        if (!table.full()) {
            for (ServerPlayerEntity player : table.players()) {
                var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
                var subtitle = translations.translateText(player, "lobby.tic_tac_toe.waiting").formatted(Formatting.GRAY, Formatting.ITALIC);

                Title.get(player).title(title, subtitle, 10, 70, 20);
            }

            return;
        }

        for (ServerPlayerEntity player : table.players()) {
            var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
            var subtitle = translations.translateText(player, "lobby.tic_tac_toe.start").formatted(Formatting.GREEN, Formatting.BOLD);

            Title.get(player).title(title, subtitle, 10, 20, 20);
        }
    }

    private void win(ServerPlayerEntity player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.you_won").formatted(Formatting.GOLD);

        Title.get(player).title(Text.empty(), subtitle, 10, 70, 20);
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1f);
    }

    public boolean isPlaying(ServerPlayerEntity player) {
        return playing.containsKey(player.getUuid());
    }

    @Nullable
    private TicTacToeTable getTableAt(BlockPos pos) {
        return tables.stream()
                .filter(table -> pos.equals(table.left()) || pos.equals(table.right()))
                .findAny().orElse(null);
    }

    public void reset() {
        playing.clear();
    }

    private static boolean isTableValid(TicTacToeTable table) {
        BlockPos left = table.left();
        BlockPos right = table.right();

        return Math.abs(left.subtract(right).getSquaredDistance(Vec3i.ZERO) - 4) < 1e-9;
    }
}

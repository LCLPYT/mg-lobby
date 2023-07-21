package work.lclpnet.lobby.decor.ttt;

import net.minecraft.block.BlockState;
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
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.title.Title;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.di.ActivityScope;
import work.lclpnet.lobby.util.WorldModifier;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ActivityScope
public class TicTacToeManager {

    private final Map<TicTacToeTable, TicTacToeInstance> tables;
    private final TranslationService translations;
    private final Scheduler scheduler;
    private final Map<UUID, TicTacToeTable> playing = new HashMap<>();
    private final TicTacToeDisplay display;

    @Inject
    public TicTacToeManager(LobbyWorldConfig config, TranslationService translations, Scheduler scheduler,
                            @Named("lobbyWorld") ServerWorld world, WorldModifier worldModifier) {
        this(getTables(config), translations, scheduler, world, worldModifier);
    }

    public TicTacToeManager(Set<TicTacToeTable> tables, TranslationService translations, Scheduler scheduler, ServerWorld world, WorldModifier worldModifier) {
        this.tables = new HashMap<>();

        for (TicTacToeTable table : tables) {
            this.tables.put(table, null);
        }

        this.scheduler = scheduler;
        this.translations = translations;
        this.display = new TicTacToeDisplay(world, worldModifier);
    }

    private static Set<TicTacToeTable> getTables(LobbyWorldConfig config) {
        return config.ticTacToeTables.stream()
                .map(TicTacToeTable::new)
                .collect(Collectors.toUnmodifiableSet());
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
        TicTacToeTable table;
        TicTacToeInstance instance;
        ServerPlayerEntity opponent;

        synchronized (this) {
            table = playing.remove(player.getUuid());
            if (table == null) return;

            int i = table.playerIndex(player);
            if (i == -1) return;

            table.player(i, null);

            instance = tables.put(table, null);

            opponent = table.opponent(i);
        }

        display.reset(table);

        if (instance != null && instance.hasBegun() && opponent != null) {
            win(opponent);
        }
    }

    private void update(TicTacToeTable table, int causePlayerId) {
        synchronized (this) {
            if (tables.get(table) != null) return;  // there is already a game at this table
        }

        if (!table.full()) {
            for (ServerPlayerEntity player : table.players()) {
                var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
                var subtitle = translations.translateText(player, "lobby.tic_tac_toe.waiting").formatted(Formatting.GRAY, Formatting.ITALIC);

                Title.get(player).title(title, subtitle, 10, 70, 20);
            }

            return;
        }

        display.reset(table);

        synchronized (this) {
            tables.put(table, createInstance(table, table.opponent(causePlayerId)));
        }

        for (ServerPlayerEntity player : table.players()) {
            var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(Formatting.AQUA);
            var subtitle = translations.translateText(player, "lobby.tic_tac_toe.start").formatted(Formatting.GREEN, Formatting.BOLD);

            Title.get(player).title(title, subtitle, 10, 20, 20);

            player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 0.6f, 0f);
        }

        display.indicateTurn(table, 1 - causePlayerId);
    }

    private TicTacToeInstance createInstance(TicTacToeTable table, ServerPlayerEntity opponent) {
        return new TicTacToeInstance(new TicTacToeGame(), table.playerIndex(opponent), new BlockState[]{
                // TODO make desired colors customizable
                Blocks.RED_GLAZED_TERRACOTTA.getDefaultState(),
                Blocks.BLUE_GLAZED_TERRACOTTA.getDefaultState()
        });
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

    private void draw(ServerPlayerEntity player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.draw").formatted(Formatting.AQUA);

        Title.get(player).title(Text.empty(), subtitle, 10, 70, 20);
        player.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.3f, 0.57f);
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

        display.displayMarker(table, x, y, instance.getDisplayBlock(playerIndex));

        if (!instance.isGameOver()) {
            display.indicateTurn(table, 1 - playerIndex);
        }

        updateInstance(table, instance);

        return true;
    }

    private void updateInstance(TicTacToeTable table, TicTacToeInstance instance) {
        if (!instance.isGameOver()) return;

        int winnerIndex = instance.getWinner();
        if (winnerIndex == -1) {
            for (ServerPlayerEntity player : table.players()) {
                draw(player);
            }

            restartGame(table);
            return;
        }

        ServerPlayerEntity winner = table.player(winnerIndex);
        win(winner);

        ServerPlayerEntity looser = table.opponent(winnerIndex);
        loose(looser);

        // TODO make combo glowing

        restartGame(table);
    }

    private void restartGame(final TicTacToeTable table) {
        final int initiator;

        synchronized (this) {
            final TicTacToeInstance instance = tables.put(table, null);

            if (instance != null) {
                initiator = instance.getInitiator();
            } else {
                initiator = 0;
            }
        }

        scheduler.timeout(() -> update(table, initiator), 120);
    }

    private Vec3i getField(Vec3d vec) {
        final double pixel = 1 / 16d;
        final double width = 3 * pixel;

        final double rx = vec.getX() - Math.floor(vec.getX());
        final double rz = vec.getZ() - Math.floor(vec.getZ());

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

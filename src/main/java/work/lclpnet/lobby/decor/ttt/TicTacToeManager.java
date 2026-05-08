package work.lclpnet.lobby.decor.ttt;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.title.Title;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.config.LobbyWorldConfig;
import work.lclpnet.lobby.util.WorldModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TicTacToeManager {

    private final Map<TicTacToeTable, TicTacToeInstance> tables;
    private final Translations translations;
    private final Scheduler scheduler;
    private final Map<UUID, TicTacToeTable> playing = new HashMap<>();
    private final TicTacToeDisplay display;

    public TicTacToeManager(LobbyWorldConfig config, Translations translations, Scheduler scheduler,
                            ServerLevel world, WorldModifier worldModifier) {
        this(getTables(config), translations, scheduler, world, worldModifier);
    }

    public TicTacToeManager(Set<TicTacToeTable> tables, Translations translations, Scheduler scheduler, ServerLevel world, WorldModifier worldModifier) {
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

    public void startPlaying(ServerPlayer player, BlockPos pos) {
        if (isPlaying(player)) return;

        final TicTacToeTable table = getTableAt(pos);

        if (table == null) return;

        final int i = table.playerIndex(pos);

        synchronized (this) {
            if (table.player(i) != null) return;  // table slot is occupied

            table.player(i, player);

            playing.put(player.getUUID(), table);
        }

        player.setGameMode(GameType.SURVIVAL);  // needed to left-click blocks
        update(table, i);
    }

    public void stopPlaying(ServerPlayer player) {
        TicTacToeTable table;
        TicTacToeInstance instance;
        ServerPlayer opponent;

        synchronized (this) {
            table = playing.remove(player.getUUID());
            if (table == null) return;

            int i = table.playerIndex(player);
            if (i == -1) return;

            table.player(i, null);

            instance = tables.put(table, null);

            opponent = table.opponent(i);
        }

        display.reset(table);
        player.setGameMode(GameType.ADVENTURE);

        if (instance != null && instance.hasBegun() && opponent != null) {
            win(opponent);
        }
    }

    private void update(TicTacToeTable table, int causePlayerId) {
        synchronized (this) {
            if (tables.get(table) != null) return;  // there is already a game at this table
        }

        if (!table.full()) {
            for (ServerPlayer player : table.players()) {
                var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(ChatFormatting.AQUA);
                var subtitle = translations.translateText(player, "lobby.tic_tac_toe.waiting").formatted(ChatFormatting.GRAY, ChatFormatting.ITALIC);

                Title.get(player).title(title, subtitle, 10, 70, 20);
            }

            return;
        }

        display.reset(table);

        synchronized (this) {
            tables.put(table, createInstance(table, table.opponent(causePlayerId)));
        }

        for (ServerPlayer player : table.players()) {
            var title = translations.translateText(player, "lobby.tic_tac_toe.title").formatted(ChatFormatting.AQUA);
            var subtitle = translations.translateText(player, "lobby.tic_tac_toe.start").formatted(ChatFormatting.GREEN, ChatFormatting.BOLD);

            Title.get(player).title(title, subtitle, 10, 20, 20);

            ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.6f, 0f);
        }

        display.indicateTurn(table, 1 - causePlayerId);
    }

    private TicTacToeInstance createInstance(TicTacToeTable table, ServerPlayer opponent) {
        return new TicTacToeInstance(new TicTacToeGame(), table.playerIndex(opponent), new BlockState[]{
                // TODO make desired colors customizable
                Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState(),
                Blocks.BLUE_GLAZED_TERRACOTTA.defaultBlockState()
        });
    }

    private void win(ServerPlayer player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.you_won").formatted(ChatFormatting.GOLD);

        Title.get(player).title(Component.empty(), subtitle, 10, 70, 20);
        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1f);
    }

    private void loose(ServerPlayer player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.you_lost").formatted(ChatFormatting.RED);

        Title.get(player).title(Component.empty(), subtitle, 10, 70, 20);
        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS, 0.6f, 1f);
    }

    private void draw(ServerPlayer player) {
        var subtitle = translations.translateText(player, "lobby.tic_tac_toe.draw").formatted(ChatFormatting.AQUA);

        Title.get(player).title(Component.empty(), subtitle, 10, 70, 20);
        ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.3f, 0.57f);
    }

    public boolean isPlaying(ServerPlayer player) {
        synchronized (this) {
            return playing.containsKey(player.getUUID());
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

    public boolean tryPlay(ServerPlayer player, BlockHitResult hitResult) {
        final TicTacToeTable table;
        final TicTacToeInstance instance;

        synchronized (this) {
            table = playing.get(player.getUUID());
            if (table == null) return false;

            BlockPos pos = hitResult.getBlockPos();
            if (!table.center().equals(pos)) return false;

            instance = tables.get(table);
        }

        if (instance == null) return false;

        final int playerIndex = table.playerIndex(player);

        if (!instance.isPlayersTurn(playerIndex)) return false;

        Vec3i field = getField(hitResult.getLocation());
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
            for (ServerPlayer player : table.players()) {
                draw(player);
            }

            restartGame(table);
            return;
        }

        ServerPlayer winner = table.player(winnerIndex);
        win(winner);

        ServerPlayer looser = table.opponent(winnerIndex);
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

    private Vec3i getField(Vec3 vec) {
        final double pixel = 1 / 16d;
        final double width = 3 * pixel;

        final double rx = vec.x() - Math.floor(vec.x());
        final double rz = vec.z() - Math.floor(vec.z());

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

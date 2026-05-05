package work.lclpnet.lobby.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.inv.prompt.OptionPrompt;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameContext;
import work.lclpnet.lobby.game.api.option.*;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.util.Interactable;
import work.lclpnet.lobby.util.Voting;

import java.util.*;
import java.util.function.Consumer;

public class LobbyWaitingManager implements GameOptionConfig, GameOptions {

    private final ServerLevel world;
    private final GameContext context;
    private final GameStarter starter;
    private final List<Voting<?>> votings = new ArrayList<>();
    private final Map<UUID, PlayerState> states = new HashMap<>();
    private final Map<Integer, Set<Runnable>> timedActions = new HashMap<>();
    private final GameManager gameManager;
    private final Consumer<Game> changeGameConsumer;

    public LobbyWaitingManager(ServerLevel world, GameContext context, GameStarter starter,
                               GameManager gameManager, Consumer<Game> changeGameConsumer) {
        this.world = world;
        this.context = context;
        this.starter = starter;
        this.gameManager = gameManager;
        this.changeGameConsumer = changeGameConsumer;
    }

    @Override
    public GameContext getContext() {
        return context;
    }

    @Override
    public synchronized <T> VotingConfig registerVoting(String name, OptionVoting<T> voting) {
        if (getVoting(name, null).isPresent()) {
            throw new IllegalArgumentException("Voting named %s already exists".formatted(name));
        }

        var impl = new Voting<>(name, voting, context.getTranslations());

        votings.add(impl);

        return ticksBeforeStart -> {
            // only one runnable should exist for each voting instance, wrap in equivalence class
            record VotingRunnable(Voting<?> voting, LobbyWaitingManager waitingManager) implements Runnable {

                @Override
                public void run() {
                    waitingManager.openVotingIfNotYetVoted(voting);
                }
            }

            addTimedAction(new VotingRunnable(impl, this), ticksBeforeStart);
        };
    }

    @Override
    public synchronized void addTimedAction(Runnable runnable, int ticksBeforeStart) {
        Objects.requireNonNull(runnable, "Action must not be null");

        if (ticksBeforeStart < 0) {
            throw new IllegalArgumentException("Ticks before start must not be negative");
        }

        Set<Runnable> actions = timedActions.computeIfAbsent(ticksBeforeStart, t -> new HashSet<>());

        actions.add(runnable);
    }

    public void runTimedActions(int ticksRemaining) {
        Set<Runnable> actions;

        synchronized (this) {
            actions = timedActions.getOrDefault(ticksRemaining, Set.of());
        }

        actions.forEach(Runnable::run);
    }

    private <T> void openVotingIfNotYetVoted(Voting<T> voting) {
        Set<UUID> voted = voting.getVoters();

        for (ServerPlayer player : world.players()) {
            // check if the player has another screen open or has already voted
            if (player.containerMenu != player.inventoryMenu || voted.contains(player.getUUID())) continue;

            voting.open(player);

            ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 0.5f, 0.5f);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Voting<T>> getVoting(String name, @Nullable Class<T> optionType) {
        return votings.stream()
                .filter(voting -> voting.getId().equals(name))
                .filter(voting -> optionType == null || voting.getData().optionType() == optionType)
                .findAny()
                .map(voting -> (Voting<T>) voting);
    }

    @Override
    public <T> Optional<VoteResult<T>> getVotingResults(String name, Class<T> optionType) {
        return this.getVoting(name, optionType)
                .map(Voting::end);
    }

    public void init(HookRegistrar hooks) {
        hooks.registerHook(PlayerConnectionHooks.JOIN, this::giveItems);
        hooks.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);
        hooks.registerHook(PlayerInteractionHooks.USE_ITEM, this::useItem);

        PlayerLookup.level(world).forEach(this::giveItems);
    }

    private void onQuit(ServerPlayer player) {
        states.remove(player.getUUID());

        removeVotesOf(player);
    }

    private void removeVotesOf(ServerPlayer player) {
        for (var voting : votings) {
            voting.removeVote(player);
        }
    }

    private InteractionResult useItem(Player _player, Level world, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(_player instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        return getState(player).onInteract(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private void giveItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        PlayerState state = getState(player);

        if (Commands.LEVEL_GAMEMASTERS.check(context.getServer().getProfilePermissions(player.nameAndId()))) {
            int gameSlot = state.getFreeSlot(8);
            inventory.setItem(gameSlot, getGameSelectorStack(player));
            state.setInteractable(gameSlot, this::openGameSelector);

            int slot = state.getFirstFreeSlot();
            inventory.setItem(slot, getStartStack(player));
            state.setInteractable(slot, _ -> startGame());
        }

        if (votings.size() == 1) {
            Voting<?> voting = votings.getFirst();

            int slot = state.getFreeSlot(4);
            inventory.setItem(slot, getStack(player, voting));
            state.setInteractable(slot, voting::open);
        } else {
            for (Voting<?> voting : votings) {
                int slot = state.getFirstFreeSlot();
                inventory.setItem(slot, getStack(player, voting));
                state.setInteractable(slot, voting::open);
            }
        }
    }

    private void startGame() {
        starter.finish(this);
    }

    private ItemStack getStack(ServerPlayer player, Voting<?> voting) {
        return voting.getData().icon().apply(player);
    }

    private ItemStack getStartStack(ServerPlayer player) {
        var stack = new ItemStack(Items.EMERALD_BLOCK);

        stack.set(DataComponents.ITEM_NAME, context.getTranslations().translateText(player, "lobby.item.start_game")
                .formatted(ChatFormatting.GREEN));

        return stack;
    }

    private ItemStack getGameSelectorStack(ServerPlayer player) {
        var stack = new ItemStack(Items.COMPASS);
        stack.set(DataComponents.ITEM_NAME, context.getTranslations().translateText(player, "lobby.item.select_game")
                .formatted(ChatFormatting.GOLD));
        return stack;
    }

    private void openGameSelector(ServerPlayer player) {
        var games = new ArrayList<>(gameManager.getGames());
        var title = context.getTranslations().translateText(player, "lobby.item.select_game")
                .formatted(ChatFormatting.GOLD);

        OptionPrompt.open(player, title, games, game -> {
            var icon = game.getConfig().icon().copy();
            icon.set(DataComponents.ITEM_NAME, context.getTranslations()
                    .translateText(player, game.getConfig().titleKey())
                    .formatted(ChatFormatting.AQUA));
            return icon;
        }).thenAccept(selected -> selected.ifPresent(game -> {
            context.getServer().execute(() -> changeGameConsumer.accept(game));
        }));
    }

    private PlayerState getState(ServerPlayer player) {
        return states.computeIfAbsent(player.getUUID(), uuid -> new PlayerState());
    }

    private static class PlayerState {
        private final Int2ObjectMap<Interactable> interactables = new Int2ObjectArrayMap<>();
        private int firstFreeSlot = 0;

        public void setInteractable(int slot, Interactable interactable) {
            interactables.put(slot, interactable);
        }

        public boolean onInteract(ServerPlayer player) {
            int slot = player.getInventory().getSelectedSlot();
            Interactable interactable = interactables.getOrDefault(slot, null);

            if (interactable == null) return false;

            interactable.onInteract(player);

            return true;

        }

        public int getFreeSlot(int preferredSlot) {
            while (interactables.containsKey(preferredSlot)) {
                preferredSlot++;
            }

            return preferredSlot;
        }

        public int getFirstFreeSlot() {
            int freeSlot = getFreeSlot(firstFreeSlot);

            firstFreeSlot = freeSlot;

            return freeSlot;
        }
    }
}

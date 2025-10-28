package work.lclpnet.lobby.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.lobby.game.api.GameContext;
import work.lclpnet.lobby.game.api.option.*;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.game.util.GameConstants;
import work.lclpnet.lobby.util.Interactable;
import work.lclpnet.lobby.util.Voting;

import java.util.*;

public class LobbyWaitingManager implements GameOptionConfig, GameOptions {

    private final ServerWorld world;
    private final GameContext context;
    private final GameStarter starter;
    private final List<Voting<?>> votings = new ArrayList<>();
    private final Map<UUID, PlayerState> states = new HashMap<>();
    private final Map<Integer, Set<Runnable>> timedActions = new HashMap<>();

    public LobbyWaitingManager(ServerWorld world, GameContext context, GameStarter starter) {
        this.world = world;
        this.context = context;
        this.starter = starter;
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

        for (ServerPlayerEntity player : world.getPlayers()) {
            // check if the player has another screen open or has already voted
            if (player.currentScreenHandler != player.playerScreenHandler || voted.contains(player.getUuid())) continue;

            voting.open(player);

            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.NEUTRAL, 0.5f, 0.5f);
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

        PlayerLookup.world(world).forEach(this::giveItems);
    }

    private void onQuit(ServerPlayerEntity player) {
        states.remove(player.getUuid());

        removeVotesOf(player);
    }

    private void removeVotesOf(ServerPlayerEntity player) {
        for (var voting : votings) {
            voting.removeVote(player);
        }
    }

    private ActionResult useItem(PlayerEntity _player, World world, Hand hand) {
        if (hand != Hand.MAIN_HAND || !(_player instanceof ServerPlayerEntity player)) {
            return ActionResult.PASS;
        }

        return getState(player).onInteract(player) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private void giveItems(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        PlayerState state = getState(player);

        if (GameConstants.DEVELOPMENT && context.getServer().getPermissionLevel(player.getPlayerConfigEntry()) >= 2) {
            Interactable startAction = p -> startGame();

            int slot = state.getFirstFreeSlot();
            inventory.setStack(slot, getStartStack(player));
            state.setInteractable(slot, startAction);
        }

        if (votings.size() == 1) {
            Voting<?> voting = votings.getFirst();

            int slot = state.getFreeSlot(4);
            inventory.setStack(slot, getStack(player, voting));
            state.setInteractable(slot, voting::open);
        } else {
            for (Voting<?> voting : votings) {
                int slot = state.getFirstFreeSlot();
                inventory.setStack(slot, getStack(player, voting));
                state.setInteractable(slot, voting::open);
            }
        }
    }

    private void startGame() {
        starter.finish(this);
    }

    private ItemStack getStack(ServerPlayerEntity player, Voting<?> voting) {
        return voting.getData().icon().apply(player);
    }

    private ItemStack getStartStack(ServerPlayerEntity player) {
        var stack = new ItemStack(Items.EMERALD_BLOCK);

        stack.set(DataComponentTypes.ITEM_NAME, context.getTranslations().translateText(player, "lobby.item.start_game")
                .formatted(Formatting.GREEN));

        return stack;
    }

    private PlayerState getState(ServerPlayerEntity player) {
        return states.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
    }

    private static class PlayerState {
        private final Int2ObjectMap<Interactable> interactables = new Int2ObjectArrayMap<>();
        private int firstFreeSlot = 0;

        public void setInteractable(int slot, Interactable interactable) {
            interactables.put(slot, interactable);
        }

        public boolean onInteract(ServerPlayerEntity player) {
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

package work.lclpnet.lobby.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.lobby.game.api.GameContext;
import work.lclpnet.lobby.game.api.option.GameOptionConfig;
import work.lclpnet.lobby.game.api.option.GameOptions;
import work.lclpnet.lobby.game.api.option.OptionVoting;
import work.lclpnet.lobby.game.api.option.VoteResult;
import work.lclpnet.lobby.util.Interactable;
import work.lclpnet.lobby.util.Voting;

import java.util.*;

public class LobbyWaitingManager implements GameOptionConfig, GameOptions {

    private final ServerWorld world;
    private final GameContext context;
    private final List<Voting<?>> votings = new ArrayList<>();
    private final Map<UUID, PlayerState> states = new HashMap<>();

    public LobbyWaitingManager(ServerWorld world, GameContext context) {
        this.world = world;
        this.context = context;
    }

    @Override
    public GameContext getContext() {
        return context;
    }

    @Override
    public synchronized <T> void registerVoting(String name, OptionVoting<T> voting) {
        if (getVoting(name, null).isPresent()) {
            throw new IllegalArgumentException("Voting named %s already exists".formatted(name));
        }

        votings.add(new Voting<>(name, voting, context.getTranslations()));
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
        hooks.registerHook(PlayerInteractionHooks.USE_ITEM, this::useItem);

        PlayerLookup.world(world).forEach(this::giveItems);
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

        if (votings.size() == 1) {
            Voting<?> voting = votings.getFirst();

            inventory.setStack(4, getStack(player, voting));
            state.setInteractable(4, voting);
            return;
        }

        int slot = 0;

        for (Voting<?> voting : votings) {
            inventory.setStack(slot, getStack(player, voting));
            state.setInteractable(slot, voting);
            slot++;
        }
    }

    private ItemStack getStack(ServerPlayerEntity player, Voting<?> voting) {
        return voting.getData().icon().apply(player);
    }

    private PlayerState getState(ServerPlayerEntity player) {
        return states.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());
    }

    private static class PlayerState {
        private final Int2ObjectMap<Interactable> interactables = new Int2ObjectArrayMap<>();

        public void setInteractable(int slot, Interactable interactable) {
            interactables.put(slot, interactable);
        }

        public boolean onInteract(ServerPlayerEntity player) {
            int slot = player.getInventory().selectedSlot;
            Interactable interactable = interactables.getOrDefault(slot, null);

            if (interactable == null) return false;

            interactable.onInteract(player);

            return true;

        }
    }
}

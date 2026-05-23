package work.lclpnet.lobby.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.api.option.Interactable;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.entity.PlayerInteractionHooks;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyPlayerStateManager {

    private final Map<UUID, PlayerState> states = new HashMap<>();

    public void init(HookRegistrar hooks) {
        hooks.registerHook(PlayerConnectionHooks.QUIT, this::cleanup);
        hooks.registerHook(PlayerInteractionHooks.USE_ITEM, this::useItem);
    }

    public synchronized PlayerState getOrCreate(ServerPlayer player) {
        return states.computeIfAbsent(player.getUUID(), _ -> new PlayerState());
    }

    @Nullable
    public synchronized PlayerState getOrNull(ServerPlayer player) {
        return states.get(player.getUUID());
    }

    private synchronized void cleanup(ServerPlayer player) {
        states.remove(player.getUUID());
    }

    private InteractionResult useItem(Player _player, Level level, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(_player instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        PlayerState state = getOrNull(player);

        if (state == null) {
            return InteractionResult.PASS;
        }

        return state.onInteract(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public synchronized void reset() {
        states.clear();
    }

    public static class PlayerState {
        private final Int2ObjectMap<Interactable> interactables = new Int2ObjectArrayMap<>();

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
    }
}

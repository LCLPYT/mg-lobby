package work.lclpnet.lobby.game.start;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.game.api.start.ItemReservationManager;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.util.LobbyPlayerStateManager;

public class GameStartItemManager {

    private final ServerLevel world;
    private final ItemReservationManager itemReservationManager;
    private final LobbyPlayerStateManager playerStateManager;
    private final Translations translations;
    private final Runnable start;

    private @Nullable ItemReservationManager.Reservation gameStartSlot = null;

    public GameStartItemManager(
            ServerLevel world,
            ItemReservationManager itemReservationManager,
            LobbyPlayerStateManager playerStateManager,
            Translations translations,
            Runnable start
    ) {
        this.world = world;
        this.itemReservationManager = itemReservationManager;
        this.playerStateManager = playerStateManager;
        this.translations = translations;
        this.start = start;
    }

    public void initGameStartItem(HookRegistrar hooks) {
        gameStartSlot = getGameStartSlot();

        hooks.registerHook(PlayerConnectionHooks.JOIN, this::giveItems);

        PlayerLookup.level(world).forEach(this::giveItems);
    }

    private @Nullable ItemReservationManager.Reservation getGameStartSlot() {
        var gameStartSlot = itemReservationManager.tryReserve(0);

        if (gameStartSlot != null) {
            return gameStartSlot;
        }

        return itemReservationManager.reserve(7);
    }

    private void giveItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        var state = playerStateManager.getOrCreate(player);

        if (!Commands.LEVEL_GAMEMASTERS.check(player.level().getServer().getProfilePermissions(player.nameAndId()))) {
            return;
        }

        if (gameStartSlot != null) {
            int slot = gameStartSlot.slot();
            inventory.setItem(slot, getStartStack(player));
            state.setInteractable(slot, _ -> start.run());
        }
    }

    private ItemStack getStartStack(ServerPlayer player) {
        var stack = new ItemStack(Items.EMERALD_BLOCK);

        stack.set(DataComponents.ITEM_NAME, translations.translateText(player, "lobby.item.start_game")
                .withStyle(ChatFormatting.GREEN));

        return stack;
    }
}

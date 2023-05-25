package work.lclpnet.lobby.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import work.lclpnet.kibu.hook.ServerPlayConnectionHooks;
import work.lclpnet.kibu.hook.entity.ServerLivingEntityHooks;
import work.lclpnet.kibu.hook.player.PlayerFoodHooks;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.hook.player.PlayerMoveCallback;
import work.lclpnet.kibu.hook.util.PositionRotation;
import work.lclpnet.kibu.hook.world.BlockModificationHooks;
import work.lclpnet.kibu.hook.world.WorldPhysicsHooks;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.api.LobbyManager;

import java.util.Set;

public class LobbyListener implements HookListenerModule {

    private final LobbyManager lobbyManager;

    public LobbyListener(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(ServerPlayConnectionHooks.JOIN, this::onJoin);
        registrar.registerHook(PlayerFoodHooks.LEVEL_CHANGE, this::onFoodLevelChange);
        registrar.registerHook(BlockModificationHooks.BREAK_BLOCK, this::onModify);
        registrar.registerHook(BlockModificationHooks.PLACE_BLOCK, this::onPlaceBlock);
        registrar.registerHook(BlockModificationHooks.PLACE_FLUID, this::onTransferFluid);
        registrar.registerHook(BlockModificationHooks.PICKUP_FLUID, this::onTransferFluid);
        registrar.registerHook(BlockModificationHooks.USE_ITEM_ON_BLOCK, this::onUseItemOnBlock);
        registrar.registerHook(BlockModificationHooks.CAN_MOB_GRIEF, (world, pos, entity) -> isLobby(world));
        registrar.registerHook(BlockModificationHooks.CHARGE_RESPAWN_ANCHOR, this::onModify);
        registrar.registerHook(BlockModificationHooks.TRAMPLE_FARMLAND, (world, pos, entity) -> isLobby(world));
        registrar.registerHook(BlockModificationHooks.COMPOSTER, this::onModify);
        registrar.registerHook(BlockModificationHooks.EAT_CAKE, this::onModify);
        registrar.registerHook(BlockModificationHooks.EXPLODE_RESPAWN_LOCATION, this::onModify);
        registrar.registerHook(BlockModificationHooks.EXTINGUISH_CANDLE, this::onModify);
        registrar.registerHook(BlockModificationHooks.PRIME_TNT, this::onModify);
        registrar.registerHook(BlockModificationHooks.TAKE_LECTERN_BOOK, this::onModify);
        registrar.registerHook(BlockModificationHooks.TRAMPLE_TURTLE_EGG, this::onModify);
        registrar.registerHook(WorldPhysicsHooks.CAULDRON_DRIP_STONE, (world, pos, newState) -> isLobby(world));
        registrar.registerHook(WorldPhysicsHooks.EXPLOSION, exploder -> isLobby(exploder.getWorld()));
        registrar.registerHook(WorldPhysicsHooks.MELT, (world, pos) -> isLobby(world));
        registrar.registerHook(WorldPhysicsHooks.FREEZE, (world, pos) -> isLobby(world));
        registrar.registerHook(WorldPhysicsHooks.SNOW_FALL, (world, pos) -> isLobby(world));
        registrar.registerHook(WorldPhysicsHooks.CAULDRON_PRECIPITATION, (world, pos, newState) -> isLobby(world));
        registrar.registerHook(WorldPhysicsHooks.FROST_WALKER_FREEZE, (world, pos, entity) -> cancelLobbyAction(entity));
        registrar.registerHook(PlayerInventoryHooks.DROP_ITEM, (player, slot) -> cancelLobbyAction(player));
        registrar.registerHook(ServerLivingEntityHooks.ALLOW_DAMAGE, this::allowDamage);
        registrar.registerHook(PlayerMoveCallback.HOOK, this::onMove);
    }

    private boolean onMove(ServerPlayerEntity player, PositionRotation from, PositionRotation to) {
        ServerWorld world = player.getWorld();

        if (isLobby(world) && to.getY() < world.getBottomY()) {
            // teleport player back to spawn location
            Vec3d spawn = lobbyManager.getLobbySpawn();

            player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0, 0);
        }

        return false;  // false = allow movement
    }

    private void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        lobbyManager.sendToLobby(handler.player);
    }

    private boolean isLobby(World world) {
        return lobbyManager.getLobbyWorld() == world;
    }

    private boolean onFoodLevelChange(PlayerEntity player, int fromLevel, int toLevel) {
        if (fromLevel <= toLevel) return false;

        return isLobby(player.getWorld());
    }

    private boolean cancelLobbyAction(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return true;  // cancel actions of non-players

        if (player.isCreativeLevelTwoOp()) return false;

        return isLobby(player.getWorld());
    }

    private boolean onPlaceBlock(World world, BlockPos pos, Entity entity, BlockState newState) {
        return cancelLobbyAction(entity);
    }

    private boolean onTransferFluid(World world, BlockPos pos, Entity entity, Fluid fluid) {
        return cancelLobbyAction(entity);
    }

    private ActionResult onUseItemOnBlock(ItemUsageContext ctx) {
        return cancelLobbyAction(ctx.getPlayer()) ? ActionResult.FAIL : null;
    }

    private boolean onModify(World world, BlockPos pos, Entity entity) {
        return cancelLobbyAction(entity);
    }

    private boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        return isLobby(entity.getWorld()) && source.isOf(DamageTypes.OUT_OF_WORLD);
    }
}

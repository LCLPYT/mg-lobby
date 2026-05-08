package work.lclpnet.lobby.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @WrapOperation(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;sendBuildLimitMessage(ZI)V",
                    ordinal = 5
            )
    )
    public void mglobby$supressBuildLimitMessageIfNeeded(ServerPlayer instance, boolean isTooHigh, int limit, Operation<Void> original) {
        // when interacting with a seat, the player awaits a position from the client
        // offhand interact will fail with an error message for out of world bounds
        // this suppresses this error message when already sitting on a seat

        if (instance.getRootVehicle() instanceof ArmorStand stand && stand.entityTags().contains("seat")) {
            return;
        }

        original.call(instance, isTooHigh, limit);
    }
}

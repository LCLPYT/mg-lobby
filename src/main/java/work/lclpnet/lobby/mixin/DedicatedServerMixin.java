package work.lclpnet.lobby.mixin;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.lobby.LobbyMod;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    @Inject(
            method = "spawnProtectionRadius",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mglobby$overrideSpawnProtectionRadius(CallbackInfoReturnable<Integer> cir) {
        LobbyMod lobbyMod = LobbyMod.optInstance();

        if (lobbyMod != null && lobbyMod.getManager().getConfig().disableSpawnProtection) {
            cir.setReturnValue(0);
        }
    }
}

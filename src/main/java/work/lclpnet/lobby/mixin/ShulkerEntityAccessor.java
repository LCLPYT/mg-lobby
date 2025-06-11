package work.lclpnet.lobby.mixin;

import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityAccessor {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Invoker
    void invokeSetColor(Optional<DyeColor> color);
}

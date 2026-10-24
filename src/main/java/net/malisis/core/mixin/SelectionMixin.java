package net.malisis.core.mixin;

import net.malisis.core.collision.GeometryIndex;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockGetter.class)
public interface SelectionMixin {
  @Inject(
      method =
          "clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;",
      at = @At("RETURN"),
      cancellable = true)
  default void malisis$selection(
      ClipContext context, CallbackInfoReturnable<BlockHitResult> result) {
    if ((Object) this instanceof Level level)
      result.setReturnValue(
          GeometryIndex.of(level)
              .clip(context.getFrom(), context.getTo(), result.getReturnValue()));
  }
}

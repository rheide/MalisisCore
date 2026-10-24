package net.malisis.core.mixin;

import net.malisis.core.collision.GeometryIndex;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CollisionGetter.class)
public interface CollisionMixin {
  @Inject(method = "getBlockCollisions", at = @At("RETURN"), cancellable = true)
  default void malisis$collisions(
      Entity entity, AABB bounds, CallbackInfoReturnable<Iterable<VoxelShape>> result) {
    if ((Object) this instanceof Level level)
      result.setReturnValue(GeometryIndex.of(level).collisions(bounds, result.getReturnValue()));
  }
}

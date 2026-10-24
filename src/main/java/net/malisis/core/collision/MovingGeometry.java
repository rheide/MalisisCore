package net.malisis.core.collision;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Loaded geometry extending beyond the anchor cell, as used by animated doors. */
public interface MovingGeometry {
  AABB coverage();

  List<AABB> collisionBoxes();

  default List<AABB> selectionBoxes() {
    return collisionBoxes();
  }

  BlockPos interactionPosition(Vec3 hit);

  boolean alive();
}

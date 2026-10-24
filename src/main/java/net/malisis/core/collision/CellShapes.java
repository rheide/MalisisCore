package net.malisis.core.collision;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

/** Keep native voxel traversal local. The spatial index handles displaced parts. */
public final class CellShapes {
  public static VoxelShape outline(List<AABB> boxes, BlockPos pos) {
    var cell = new AABB(pos);
    var shape = Shapes.empty();
    for (var box : boxes)
      if (box.intersects(cell)) {
        var clipped = box.intersect(cell).move(-pos.getX(), -pos.getY(), -pos.getZ());
        shape = Shapes.or(shape, Shapes.create(clipped));
      }
    return shape;
  }
}

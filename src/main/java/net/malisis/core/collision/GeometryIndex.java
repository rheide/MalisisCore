package net.malisis.core.collision;

import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Chunk buckets keep collision/ray queries local; world ownership is weak and unload clears it. */
public final class GeometryIndex {
  private static final Map<Level, GeometryIndex> WORLDS =
      Collections.synchronizedMap(new WeakHashMap<>());
  private final Map<MovingGeometry, AABB> registered = new IdentityHashMap<>();
  private final Map<Long, Set<MovingGeometry>> buckets = new HashMap<>();

  public static GeometryIndex of(Level level) {
    return WORLDS.computeIfAbsent(level, k -> new GeometryIndex());
  }

  public static void clear(Level level) {
    WORLDS.remove(level);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static int chunk(double p) {
    return ((int) Math.floor(p)) >> 4;
  }

  public void add(MovingGeometry geometry) {
    var bounds = geometry.coverage();
    if (bounds.equals(registered.get(geometry))) return;
    remove(geometry);
    registered.put(geometry, bounds);
    for (int x = chunk(bounds.minX); x <= chunk(bounds.maxX); x++)
      for (int z = chunk(bounds.minZ); z <= chunk(bounds.maxZ); z++)
        buckets
            .computeIfAbsent(key(x, z), k -> Collections.newSetFromMap(new IdentityHashMap<>()))
            .add(geometry);
  }

  public void remove(MovingGeometry geometry) {
    var bounds = registered.remove(geometry);
    if (bounds == null) return;
    for (int x = chunk(bounds.minX); x <= chunk(bounds.maxX); x++)
      for (int z = chunk(bounds.minZ); z <= chunk(bounds.maxZ); z++) {
        long k = key(x, z);
        var set = buckets.get(k);
        if (set != null) {
          set.remove(geometry);
          if (set.isEmpty()) buckets.remove(k);
        }
      }
  }

  private java.util.Collection<MovingGeometry> candidates(AABB box) {
    if (chunk(box.minX) == chunk(box.maxX) && chunk(box.minZ) == chunk(box.maxZ)) {
      var bucket = buckets.get(key(chunk(box.minX), chunk(box.minZ)));
      return bucket == null ? Collections.emptySet() : bucket;
    }
    Set<MovingGeometry> result = null;
    for (int x = chunk(box.minX); x <= chunk(box.maxX); x++)
      for (int z = chunk(box.minZ); z <= chunk(box.maxZ); z++) {
        var bucket = buckets.get(key(x, z));
        if (bucket != null) {
          if (result == null) result = Collections.newSetFromMap(new IdentityHashMap<>());
          result.addAll(bucket);
        }
      }
    return result == null ? Collections.emptySet() : result;
  }

  public Iterable<VoxelShape> collisions(AABB box, Iterable<VoxelShape> original) {
    var candidates = candidates(box);
    if (candidates.isEmpty()) return original;
    List<VoxelShape> extra = new ArrayList<>();
    for (var geometry : candidates)
      if (geometry.alive() && geometry.coverage().intersects(box))
        for (AABB shape : geometry.collisionBoxes())
          if (shape.intersects(box)) extra.add(Shapes.create(shape));
    if (extra.isEmpty()) return original;
    return () ->
        new Iterator<>() {
          final Iterator<VoxelShape> a = original.iterator(), b = extra.iterator();

          public boolean hasNext() {
            return a.hasNext() || b.hasNext();
          }

          public VoxelShape next() {
            return a.hasNext() ? a.next() : b.next();
          }
        };
  }

  public BlockHitResult clip(Vec3 start, Vec3 end, BlockHitResult original) {
    double nearest =
        original.getType() == HitResult.Type.MISS
            ? Double.POSITIVE_INFINITY
            : start.distanceToSqr(original.getLocation());
    BlockHitResult result = original;
    AABB ray = new AABB(start, end).inflate(.001);
    for (var geometry : candidates(ray))
      if (geometry.alive())
        for (var box : geometry.selectionBoxes()) {
          var optional = box.clip(start, end);
          if (optional.isEmpty()) continue;
          var point = optional.get();
          double distance = start.distanceToSqr(point);
          if (distance >= nearest) continue;
          nearest = distance;
          Direction face = face(box, point);
          result =
              new BlockHitResult(
                  point, face, geometry.interactionPosition(point), box.contains(start));
        }
    return result;
  }

  private static Direction face(AABB b, Vec3 p) {
    double[] d = {
      Math.abs(p.y - b.minY),
      Math.abs(p.y - b.maxY),
      Math.abs(p.z - b.minZ),
      Math.abs(p.z - b.maxZ),
      Math.abs(p.x - b.minX),
      Math.abs(p.x - b.maxX)
    };
    int best = 0;
    for (int i = 1; i < 6; i++) if (d[i] < d[best]) best = i;
    return Direction.from3DDataValue(best);
  }
}

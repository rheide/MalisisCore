package net.malisis.core.checks;

import net.malisis.core.animation.DoorTimeline;
import net.malisis.core.util.BoundedCache;

public final class CoreChecks {
  static int checks;

  static void near(float actual, float expected) {
    checks++;
    if (Math.abs(actual - expected) > .00001f) throw new AssertionError(actual + " != " + expected);
  }

  public static void main(String[] args) {
    DoorTimeline motion = new DoorTimeline();
    near(motion.update(false, 0, 6), 0);
    near(motion.update(true, 0, 6), 0);
    near(motion.update(true, 3, 6), .5f);
    near(motion.update(false, 3, 6), .5f);
    near(motion.update(false, 4, 6), 1f / 3);
    near(motion.update(false, 6, 6), 0);
    near(motion.update(true, 6, 6), 0);
    near(motion.update(true, 12, 6), 1);
    near(motion.update(false, 12, 6), 1);
    near(motion.update(false, 15, 6), .5f);
    near(motion.update(true, 15, 6), .5f);
    near(motion.update(true, 18, 6), 1);
    motion.reset(true, 100);
    near(motion.update(true, 100, 6), 1);
    near(motion.update(false, 100, 6), 1);
    near(motion.update(false, 106, 6), 0);
    BoundedCache<Integer, Object> cache = new BoundedCache<>(2);
    Object a = cache.get(1, k -> new Object()), b = cache.get(2, k -> new Object());
    checks++;
    if (cache.get(1, k -> new Object()) != a) throw new AssertionError("cache miss on live key");
    cache.get(3, k -> new Object());
    checks++;
    if (cache.size() != 2) throw new AssertionError("unbounded cache");
    checks++;
    if (cache.get(2, k -> new Object()) == b) throw new AssertionError("wrong eviction order");
    cache.clear();
    checks++;
    if (cache.size() != 0) throw new AssertionError("clear");
    System.out.println("PASS CoreChecks " + checks + " timeline reversal/cache checks");
  }
}

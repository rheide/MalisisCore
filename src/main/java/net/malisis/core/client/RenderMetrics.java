package net.malisis.core.client;

import java.lang.management.ManagementFactory;

/** Disabled unless explicitly requested by the private rendering harness. */
public final class RenderMetrics {
  public static final boolean ENABLED = Boolean.getBoolean("malisis.metrics");
  public static boolean active;
  public static long nanos, bytes, calls, vertices;
  private static long allocation;
  private static final com.sun.management.ThreadMXBean MEMORY =
      ENABLED ? (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean() : null;

  public static void reset() {
    nanos = bytes = calls = vertices = 0;
    active = true;
  }

  public static long begin() {
    if (!active) return 0;
    allocation = MEMORY.getThreadAllocatedBytes(Thread.currentThread().threadId());
    return System.nanoTime();
  }

  public static void end(long start) {
    if (start == 0) return;
    nanos += System.nanoTime() - start;
    bytes += MEMORY.getThreadAllocatedBytes(Thread.currentThread().threadId()) - allocation;
    calls++;
  }
}

package net.malisis.core.animation;

/** Reusable, allocation-free linear animation. Reversals begin at the current pose. */
public final class DoorTimeline {
  private boolean initialized;
  private float from, target;
  private double started, duration;

  public float update(boolean open, double time, int fullTravelTicks) {
    float next = open ? 1 : 0;
    if (!initialized) {
      initialized = true;
      from = target = next;
      started = time;
      duration = 0;
      return next;
    }
    if (next != target) {
      from = sample(time);
      target = next;
      started = time;
      duration = Math.abs(target - from) * Math.max(1, fullTravelTicks);
    }
    return sample(time);
  }

  public float sample(double time) {
    if (duration <= 0) return target;
    float t = (float) Math.max(0, Math.min(1, (time - started) / duration));
    return from + (target - from) * t;
  }

  public void reset(boolean open, double time) {
    initialized = true;
    from = target = open ? 1 : 0;
    started = time;
    duration = 0;
  }
}

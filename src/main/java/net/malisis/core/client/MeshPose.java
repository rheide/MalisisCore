package net.malisis.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

/** Renderer-owned scratch pose avoids per-part PoseStack copies and temporary normal matrices. */
public final class MeshPose {
  private final PoseStack.Pose pose = new PoseStack().last();

  public PoseStack.Pose set(PoseStack.Pose parent, Matrix4f transform) {
    pose.pose().set(parent.pose()).mul(transform);
    pose.normal().set(pose.pose()).invert().transpose();
    return pose;
  }
}

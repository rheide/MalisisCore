package net.malisis.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Immutable 24-vertex panel. UVs/positions are built once, never reconstructed per frame. */
public final class BoxMesh {
  private final float[] vertices;

  private BoxMesh(float[] data) {
    vertices = data;
  }

  public static BoxMesh of(float[] data) {
    if (data.length % 32 != 0) throw new IllegalArgumentException("Expected complete quads");
    return new BoxMesh(data.clone());
  }

  public static BoxMesh panel(
      TextureAtlasSprite front, TextureAtlasSprite side, boolean upper, boolean mirror) {
    float d = 3f / 16 * .995f;
    float[][] corners = {
      {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, d}, {1, 0, d}, {1, 1, d}, {0, 1, d}
    };
    int[][] faces = {
      {1, 0, 3, 2}, {4, 5, 6, 7}, {0, 4, 7, 3}, {5, 1, 2, 6}, {3, 7, 6, 2}, {0, 1, 5, 4}
    };
    float[][] normals = {{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, -1, 0}};
    float[] data = new float[24 * 8];
    int offset = 0;
    for (int f = 0; f < 6; f++)
      for (int v = 0; v < 4; v++) {
        float[] c = corners[faces[f][v]];
        float u = v == 0 || v == 3 ? 0 : 1;
        float vv = v < 2 ? 1 : 0;
        TextureAtlasSprite texture = f < 2 ? front : side;
        if (f < 2 && mirror) u = 1 - u;
        if (f >= 2) {
          int strip = f >= 4 ? 0 : (upper ? 1 : 3) + (f == 3 ? 1 : 0);
          u = (strip + u) * 3f / 16;
        }
        data[offset++] = c[0];
        data[offset++] = c[1];
        data[offset++] = c[2];
        data[offset++] = texture.getU(u);
        data[offset++] = texture.getV(vv);
        data[offset++] = normals[f][0];
        data[offset++] = normals[f][1];
        data[offset++] = normals[f][2];
      }
    return new BoxMesh(data);
  }

  public void emit(PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay) {
    emit(pose, buffer, light, overlay, 0xffffff, 255);
  }

  public void emit(
      PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color, int alpha) {
    emitTaperedX(pose, buffer, light, overlay, color, alpha, 1, 1, 0);
  }

  public void emitTaperedX(
      PoseStack.Pose pose,
      VertexConsumer buffer,
      int light,
      int overlay,
      int color,
      int alpha,
      float bottomWidth,
      float topWidth,
      float pivotX) {
    if (RenderMetrics.ENABLED && RenderMetrics.active) RenderMetrics.vertices += vertexCount();
    var matrix = pose.pose();
    var normal = pose.normal();
    for (int i = 0; i < vertices.length; i += 8) {
      float x = vertices[i],
          y = vertices[i + 1],
          z = vertices[i + 2],
          a = vertices[i + 5],
          b = vertices[i + 6],
          c = vertices[i + 7];
      x = pivotX + (x - pivotX) * (bottomWidth + (topWidth - bottomWidth) * y);
      float nx = normal.m00() * a + normal.m10() * b + normal.m20() * c,
          ny = normal.m01() * a + normal.m11() * b + normal.m21() * c,
          nz = normal.m02() * a + normal.m12() * b + normal.m22() * c;
      float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
      buffer
          .addVertex(
              matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30(),
              matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31(),
              matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32())
          .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha)
          .setUv(vertices[i + 3], vertices[i + 4])
          .setOverlay(overlay)
          .setLight(light)
          .setNormal(nx / length, ny / length, nz / length);
    }
  }

  public static BoxMesh cuboid(
      float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite texture) {
    BoxMesh unit = panel(texture, texture, false, false);
    float[] data = unit.vertices.clone();
    float depth = 3f / 16 * .995f;
    for (int i = 0; i < data.length; i += 8) {
      data[i] = x0 + data[i] * (x1 - x0);
      data[i + 1] = y0 + data[i + 1] * (y1 - y0);
      data[i + 2] = z0 + data[i + 2] / depth * (z1 - z0);
    }
    return new BoxMesh(data);
  }

  public static BoxMesh cuboidUV(
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      TextureAtlasSprite texture,
      float u0,
      float u1) {
    BoxMesh mesh = cuboid(x0, y0, z0, x1, y1, z1, texture);
    float[] data = mesh.vertices.clone();
    for (int i = 0; i < 8; i++) {
      int offset = i * 8;
      // Both faces share texture X across the complete double door. Reversing
      // the north face independently puts its handles at the outer hinges.
      float u = i < 4 ? (i % 4 == 0 || i % 4 == 3 ? u1 : u0) : (i % 4 == 0 || i % 4 == 3 ? u0 : u1);
      data[offset + 3] = texture.getU(u);
    }
    return new BoxMesh(data);
  }

  public BoxMesh faces(int mask) {
    if (vertices.length != 24 * 8) throw new IllegalStateException("Expected cuboid mesh");
    float[] data = new float[Integer.bitCount(mask & 63) * 32];
    int destination = 0;
    for (int face = 0; face < 6; face++)
      if ((mask & (1 << face)) != 0) {
        System.arraycopy(vertices, face * 32, data, destination, 32);
        destination += 32;
      }
    return new BoxMesh(data);
  }

  public static BoxMesh merge(BoxMesh... meshes) {
    int size = 0;
    for (BoxMesh mesh : meshes) size += mesh.vertices.length;
    float[] data = new float[size];
    int offset = 0;
    for (BoxMesh mesh : meshes) {
      System.arraycopy(mesh.vertices, 0, data, offset, mesh.vertices.length);
      offset += mesh.vertices.length;
    }
    return new BoxMesh(data);
  }

  public static BoxMesh blockCuboid(
      float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite texture) {
    BoxMesh mesh = cuboid(x0, y0, z0, x1, y1, z1, texture);
    float[] data = mesh.vertices.clone();
    for (int i = 0; i < data.length; i += 8) {
      int face = i / 32;
      float x = data[i], y = data[i + 1], z = data[i + 2], u, v;
      if (face < 2) {
        u = face == 0 ? 1 - x : x;
        v = 1 - y;
      } else if (face < 4) {
        u = face == 2 ? z : 1 - z;
        v = 1 - y;
      } else {
        u = x;
        v = face == 4 ? z : 1 - z;
      }
      data[i + 3] = texture.getU(u);
      data[i + 4] = texture.getV(v);
    }
    return new BoxMesh(data);
  }

  public int vertexCount() {
    return vertices.length / 8;
  }
}

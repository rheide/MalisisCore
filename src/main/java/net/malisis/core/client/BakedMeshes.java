package net.malisis.core.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Decode the closed vanilla baked model once; moving renders reuse its original UVs and geometry.
 */
public final class BakedMeshes {
  private BakedMeshes() {}

  public static BoxMesh model(BlockState state) {
    var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    List<BakedQuad> quads = new ArrayList<>();
    for (Direction side : Direction.values())
      quads.addAll(model.getQuads(state, side, RandomSource.create(0)));
    quads.addAll(model.getQuads(state, null, RandomSource.create(0)));
    float[] vertices = new float[quads.size() * 32];
    int offset = 0;
    for (var quad : quads) {
      int[] data = quad.getVertices();
      int stride = data.length / 4;
      var normal = quad.getDirection();
      for (int vertex = 0; vertex < 4; vertex++) {
        int i = vertex * stride;
        vertices[offset++] = Float.intBitsToFloat(data[i]);
        vertices[offset++] = Float.intBitsToFloat(data[i + 1]);
        vertices[offset++] = Float.intBitsToFloat(data[i + 2]);
        vertices[offset++] = Float.intBitsToFloat(data[i + 4]);
        vertices[offset++] = Float.intBitsToFloat(data[i + 5]);
        vertices[offset++] = normal.getStepX();
        vertices[offset++] = normal.getStepY();
        vertices[offset++] = normal.getStepZ();
      }
    }
    return BoxMesh.of(vertices);
  }
}

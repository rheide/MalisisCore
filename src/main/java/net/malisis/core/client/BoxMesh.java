package net.malisis.core.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Immutable 24-vertex panel. UVs/positions are built once, never reconstructed per frame. */
public final class BoxMesh {
 private final float[] vertices;
 private BoxMesh(float[] data){vertices=data;}
 public static BoxMesh panel(TextureAtlasSprite front,TextureAtlasSprite side,boolean upper,boolean mirror) {
  float d=3f/16*.995f;
  float[][] corners={{0,0,0},{1,0,0},{1,1,0},{0,1,0},{0,0,d},{1,0,d},{1,1,d},{0,1,d}};
  int[][] faces={{1,0,3,2},{4,5,6,7},{0,4,7,3},{5,1,2,6},{3,7,6,2},{0,1,5,4}};
  float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};
  float[] data=new float[24*8];int offset=0;
  for(int f=0;f<6;f++)for(int v=0;v<4;v++) {
   float[] c=corners[faces[f][v]];float u=v==0||v==3?0:1;float vv=v<2?1:0;
   TextureAtlasSprite texture=f<2?front:side;
   if(f<2&&mirror)u=1-u;
   if(f>=2){int strip=f>=4?0:(upper?1:3)+(f==3?1:0);u=(strip+u)*3f/16;}
   data[offset++]=c[0];data[offset++]=c[1];data[offset++]=c[2];
   data[offset++]=texture.getU(u);data[offset++]=texture.getV(vv);
   data[offset++]=normals[f][0];data[offset++]=normals[f][1];data[offset++]=normals[f][2];
  }
  return new BoxMesh(data);
 }
 public void emit(PoseStack.Pose pose,VertexConsumer buffer,int light,int overlay) {
  var matrix=pose.pose();var normal=pose.normal();
  for(int i=0;i<vertices.length;i+=8){
   float x=vertices[i],y=vertices[i+1],z=vertices[i+2],a=vertices[i+5],b=vertices[i+6],c=vertices[i+7];
   float nx=normal.m00()*a+normal.m10()*b+normal.m20()*c,ny=normal.m01()*a+normal.m11()*b+normal.m21()*c,nz=normal.m02()*a+normal.m12()*b+normal.m22()*c;
   float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
   buffer.addVertex(matrix.m00()*x+matrix.m10()*y+matrix.m20()*z+matrix.m30(),matrix.m01()*x+matrix.m11()*y+matrix.m21()*z+matrix.m31(),matrix.m02()*x+matrix.m12()*y+matrix.m22()*z+matrix.m32()).setColor(255,255,255,255).setUv(vertices[i+3],vertices[i+4]).setOverlay(overlay).setLight(light).setNormal(nx/length,ny/length,nz/length);
  }
 }
 public int vertexCount(){return vertices.length/8;}
}

package net.malisis.core.model;
import java.io.*;
import java.util.*;
import net.malisis.core.client.BoxMesh;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Original OBJ groups are parsed once per resource generation into immutable native meshes. */
public final class ObjMesh {
 private ObjMesh(){}
 public static Map<String,BoxMesh> read(Reader reader,TextureAtlasSprite sprite)throws IOException {
  List<float[]> positions=new ArrayList<>(),uvs=new ArrayList<>();Map<String,List<Float>> parts=new LinkedHashMap<>();String name="default";parts.put(name,new ArrayList<>());
  try(BufferedReader input=new BufferedReader(reader)){String line;while((line=input.readLine())!=null){String[] fields=line.trim().split("\\s+");if(fields.length==0)continue;
   switch(fields[0]){
    case "v"->positions.add(new float[]{Float.parseFloat(fields[1]),Float.parseFloat(fields[2]),Float.parseFloat(fields[3])});
    case "vt"->uvs.add(new float[]{Float.parseFloat(fields[1]),1-Float.parseFloat(fields[2])});
    case "o","g"->{name=fields[1].toLowerCase(Locale.ROOT).split("_")[0];parts.computeIfAbsent(name,k->new ArrayList<>());}
    case "f"->{
     int count=fields.length-1;
     if(count==4)quad(parts.get(name),new String[]{fields[1],fields[2],fields[3],fields[4]},positions,uvs,sprite);
     else for(int i=2;i<count;i++)quad(parts.get(name),new String[]{fields[1],fields[i],fields[i+1],fields[i+1]},positions,uvs,sprite);
    }
   }
  }}
  Map<String,BoxMesh> result=new LinkedHashMap<>();parts.forEach((part,data)->{if(!data.isEmpty()){float[] packed=new float[data.size()];for(int i=0;i<packed.length;i++)packed[i]=data.get(i);result.put(part,BoxMesh.of(packed));}});return Collections.unmodifiableMap(result);
 }
 private static int index(String value,int size){int i=Integer.parseInt(value);return i<0?size+i:i-1;}
 private static void quad(List<Float> out,String[] refs,List<float[]> positions,List<float[]> uvs,TextureAtlasSprite sprite){
  float[][] xyz=new float[4][],uv=new float[4][];
  for(int i=0;i<4;i++){String[] ref=refs[i].split("/");xyz[i]=positions.get(index(ref[0],positions.size()));uv[i]=ref.length>1&&!ref[1].isEmpty()?uvs.get(index(ref[1],uvs.size())):new float[]{0,0};}
  float ax=xyz[1][0]-xyz[0][0],ay=xyz[1][1]-xyz[0][1],az=xyz[1][2]-xyz[0][2],bx=xyz[2][0]-xyz[0][0],by=xyz[2][1]-xyz[0][1],bz=xyz[2][2]-xyz[0][2];
  float nx=ay*bz-az*by,ny=az*bx-ax*bz,nz=ax*by-ay*bx,length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(length==0)length=1;
  for(int i=0;i<4;i++){for(float v:xyz[i])out.add(v);out.add(sprite.getU(uv[i][0]));out.add(sprite.getV(uv[i][1]));out.add(nx/length);out.add(ny/length);out.add(nz/length);}
 }
}

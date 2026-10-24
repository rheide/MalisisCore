package net.malisis.core.checks;
import net.malisis.core.renderer.*;
import net.malisis.core.renderer.Parameter;
import net.malisis.core.renderer.element.Vertex;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/** Uses unchanged runtime bytecode and its clone/merge contract as independent oracles. */
public final class RenderParityChecks {
 static int checked;
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);checked++;}
 @SuppressWarnings({"rawtypes","unchecked"}) static void randomize(RenderParameters p,Random rng)throws Exception {
  for(Field f:RenderParameters.class.getFields())if(f.getType()==Parameter.class){Parameter v=(Parameter)f.get(p);Object def=v.getDefault();Object value=null;
   if(def instanceof Boolean)value=rng.nextBoolean();else if(def instanceof Integer)value=rng.nextInt();else if(def instanceof Float)value=rng.nextFloat();
   else if(f.getName().equals("direction")||f.getName().equals("textureSide"))value=EnumFacing.values()[rng.nextInt(6)];
   else if(f.getName().equals("aoMatrix"))value=new int[][][]{{{rng.nextInt(5),rng.nextInt(5),rng.nextInt(5)}}};
   else if(f.getName().equals("renderBounds"))value=new AxisAlignedBB(0,0,0,rng.nextDouble(),rng.nextDouble(),rng.nextDouble());
   if(rng.nextBoolean())v.set(value);
  }
 }
 static void parameters()throws Exception{
  Random rng=new Random(738001);RenderParameters scratch=new RenderParameters();
  for(int i=0;i<5000;i++){
   RenderParameters base=i%7==0?null:new RenderParameters(),over=i%11==0?null:new RenderParameters();
   if(base!=null){randomize(base,rng);base.colorFactor=new Parameter<>(rng.nextFloat());}
   if(over!=null)randomize(over,rng);
   RenderParameters expected=RenderParameters.merge(base,over);scratch.copyMerged(base,over);
   for(Field f:RenderParameters.class.getFields())if(f.getType()==Parameter.class){Parameter<?> a=(Parameter<?>)f.get(expected),b=(Parameter<?>)f.get(scratch);
    require(Objects.deepEquals(a.getDefault(),b.getDefault()),"default "+f.getName());require(Objects.deepEquals(a.getValue(),b.getValue()),"override "+f.getName());require(Objects.deepEquals(a.get(),b.get()),"effective "+f.getName());
   }
  }
 }
 static void vertices(String reference)throws Exception{
  final URL url=Paths.get(reference).toUri().toURL();ClassLoader parent=RenderParityChecks.class.getClassLoader();
  try(URLClassLoader loader=new URLClassLoader(new URL[]{url},parent){
   protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
    if(!name.equals("net.malisis.core.renderer.element.Vertex"))return super.loadClass(name,resolve);
    synchronized(getClassLoadingLock(name)){Class<?> c=findLoadedClass(name);if(c==null)c=findClass(name);if(resolve)resolveClass(c);return c;}
   }
  }){
   Class<?> original=loader.loadClass("net.malisis.core.renderer.element.Vertex");Constructor<?> ctor=original.getConstructor(double.class,double.class,double.class);Method pack=original.getMethod("getVertexData",VertexFormat.class,Vec3d.class);
   Method uv=original.getMethod("setUV",double.class,double.class),color=original.getMethod("setColor",int.class),alpha=original.getMethod("setAlpha",int.class),light=original.getMethod("setBrightness",int.class),normal=original.getMethod("setNormal",EnumFacing.class);
   VertexFormat[] formats={DefaultVertexFormats.BLOCK,DefaultVertexFormats.ITEM,MalisisRenderer.malisisVertexFormat,new VertexFormat(DefaultVertexFormats.BLOCK).addElement(new VertexFormatElement(2,VertexFormatElement.EnumType.FLOAT,VertexFormatElement.EnumUsage.UV,2))};
   Random rng=new Random(738002);
   for(int i=0;i<10000;i++){
    double x=(rng.nextDouble()-.5)*100000,y=(rng.nextDouble()-.5)*100,z=(rng.nextDouble()-.5)*100000;Vertex v=new Vertex(x,y,z);Object ref=ctor.newInstance(x,y,z);
    double u=rng.nextDouble(),w=rng.nextDouble();int c=rng.nextInt(),a=rng.nextInt(256),l=rng.nextInt();EnumFacing n=EnumFacing.values()[i%6];
    v.setUV(u,w);v.setColor(c);v.setAlpha(a);v.setBrightness(l);v.setNormal(n);uv.invoke(ref,u,w);color.invoke(ref,c);alpha.invoke(ref,a);light.invoke(ref,l);normal.invoke(ref,n);
    Vec3d offset=i%3==0?null:new Vec3d(rng.nextDouble()*1000,rng.nextDouble()*100,rng.nextDouble()*1000);
    for(VertexFormat format:formats){int[] data=new int[format.getIntegerSize()];Arrays.fill(data,0xdeadbeef);v.writeVertexData(format,offset,data);require(Arrays.equals((int[])pack.invoke(ref,format,offset),data),"vertex record parity "+i+" "+format);}
   }
  }
 }
 public static void main(String[] args)throws Exception{parameters();vertices(args[0]);System.out.println("PASS RenderParityChecks "+checked+" independent parameter/vertex comparisons");}
}

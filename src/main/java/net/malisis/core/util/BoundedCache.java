package net.malisis.core.util;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
/** Owner-thread LRU; the owner clears resource-dependent entries on resource reload. */
public final class BoundedCache<K,V> {
 private final int limit;
 private final Map<K,V> entries=new LinkedHashMap<>(32,.75f,true);
 public BoundedCache(int limit){if(limit<1)throw new IllegalArgumentException("limit");this.limit=limit;}
 public V get(K key,Function<K,V> factory){V value=entries.get(key);if(value==null){value=factory.apply(key);entries.put(key,value);if(entries.size()>limit)entries.remove(entries.keySet().iterator().next());}return value;}
 public void clear(){entries.clear();}
 public int size(){return entries.size();}
}

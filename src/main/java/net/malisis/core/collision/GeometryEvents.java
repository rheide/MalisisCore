package net.malisis.core.collision;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
@EventBusSubscriber(modid="malisiscore")
public final class GeometryEvents { @SubscribeEvent public static void unload(LevelEvent.Unload event){if(event.getLevel() instanceof Level level)GeometryIndex.clear(level);} }

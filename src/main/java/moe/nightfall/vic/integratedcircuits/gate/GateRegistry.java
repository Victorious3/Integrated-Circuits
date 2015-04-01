package moe.nightfall.vic.integratedcircuits.gate;

import java.util.HashMap;

import moe.nightfall.vic.integratedcircuits.client.IPartRenderer;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateRegistry
{
	private static HashBiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private static HashMap<Class<?>, IPartRenderer<?>> rendererRegistry = Maps.newHashMap();
	
	private GateRegistry() {}
	
	public static void registerGate(String name, Class<? extends IGate> clazz)
	{
		registry.put(name, clazz);
	}
	
	@SideOnly(Side.CLIENT)
	public static <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer)
	{
		rendererRegistry.put(clazz, renderer);
	}
	
	@SideOnly(Side.CLIENT)
	public static IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz) 
	{
		return (IPartRenderer<IGate>) rendererRegistry.get(clazz);
	}
	
	@SideOnly(Side.CLIENT)
	public static IPartRenderer<IGate> getRenderer(String gateID) 
	{
		return getRenderer(registry.get(gateID));
	}
	
	public static String getName(Class<? extends IGate> gate)
	{
		return registry.inverse().get(gate);
	}
	
	public static IGate createGateInstace(String name)
	{
		try {
			return registry.get(name).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Coundn't instance gate \"" + name + "\", need an empty constructor!");
		}
	}
}

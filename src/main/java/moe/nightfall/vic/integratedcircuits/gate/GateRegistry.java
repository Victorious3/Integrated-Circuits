package moe.nightfall.vic.integratedcircuits.gate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI.Type;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.client.SocketRenderer;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import moe.nightfall.vic.integratedcircuits.tile.FMPartSocket;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GateRegistry implements IGateRegistry {
	
	private boolean lock = false;
	
	private BiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private Map<Type, Set<GateIOProvider>> ioProviderRegistry = Maps.newHashMap();

	private Map<Type, Set<Class<?>>> interfaceMap;
	private Map<Class<?>, GateIOProvider> ioProviderMap;

	public GateRegistry() {

	}

	/**
	 * Invoked by ASM
	 */
	public GateIOProvider getProvider(Class<?> intf) {
		return ioProviderMap.get(intf);
	}

	public void lock() {
		if(!lock) {

			IntegratedCircuits.logger.info("Locking IO provider registry and building caches");

			// Building caches

			ioProviderMap = Maps.newHashMap();
			interfaceMap = Maps.newEnumMap(Type.class);

			for (Type type : ioProviderRegistry.keySet()) {
				Set<GateIOProvider> ioProviderSet = ioProviderRegistry.get(type);
				for (GateIOProvider provider : ioProviderSet) {
					Interface intf = provider.getClass().getAnnotation(Interface.class);
					if (intf != null)
						addInterface(intf, provider, type);
					InterfaceList intfList = provider.getClass().getAnnotation(InterfaceList.class);
					if (intfList != null) {
						for (Interface intf2 : intfList.value()) {
							addInterface(intf2, provider, type);
						}
					}
				}
			}
		}
		lock = true;
	}

	private void addInterface(Interface intf, GateIOProvider provider, Type type) {
		if (Loader.isModLoaded(intf.modid())) {
			try {
				Class<?> intfClazz = Class.forName(intf.iface());
				ioProviderMap.put(intfClazz, provider);
				Set<Class<?>> set = interfaceMap.get(type);
				set = set == null ? new HashSet<Class<?>>() : set;
				set.add(intfClazz);
				interfaceMap.put(type, set);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Invalid interface specified on io provider " + provider.getClass() + " : " + intf.iface());
			}
		}
	}

	@Override
	public void registerGate(String name, Class<? extends IGate> clazz) {
		registry.put(name, clazz);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer) {
		ClientProxy.rendererRegistry.put(clazz, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz) {
		return (IPartRenderer<IGate>) ClientProxy.rendererRegistry.get(clazz);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(String gateID) {
		return getRenderer(registry.get(gateID));
	}

	@Override
	public String getName(Class<? extends IGate> gate) {
		return registry.inverse().get(gate);
	}

	@Override
	public IGate createGateInstace(String name) {
		try {
			return registry.get(name).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Coundn't instance gate \"" + name + "\", need an empty constructor!");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<ISocket> createDefaultSocketRenderer(String iconName) {
		ClientProxy.icons.add(iconName);
		return new SocketRenderer(iconName);
	}

	@Override
	public void registerGateIOProvider(GateIOProvider provider, Type... elements) {
		for (Type element : elements)
			registerGateIOProvider(provider, element);
	}

	private void registerGateIOProvider(GateIOProvider provider, Type element) {
		if (lock) {
			IntegratedCircuits.logger.fatal("Tried to register gate provider instance after initialization phase: "+ Loader.instance().activeModContainer());
			return;
		}
		Set<GateIOProvider> set = ioProviderRegistry.get(element);
		if (set == null)
			set = Sets.newHashSet();
		set.add(provider);
		ioProviderRegistry.put(element, set);
	}

	public Set<GateIOProvider> getIOProviderList(Type type) {
		return ioProviderRegistry.get(type);
	}

	public Set<GateIOProvider> getIOProviderList(Class<?> clazz) {
		if (clazz == TileEntitySocket.class)
			return ioProviderRegistry.get(Type.TILE);
		if (clazz == Block.class)
			return ioProviderRegistry.get(Type.BLOCK);
		if (IntegratedCircuits.isFMPLoaded && clazz == FMPartSocket.class)
			return ioProviderRegistry.get(Type.TILE_FMP);
		return null;
	}

	public Set<Class<?>> getInterfaceMapping(Type type) {
		Set<Class<?>> set = interfaceMap.get(type);
		return set != null ? set : new HashSet<Class<?>>();
	}

	@Override
	public ISocket createSocketInstance(ISocketWrapper wrapper) {
		return new Socket(wrapper);
	}
}

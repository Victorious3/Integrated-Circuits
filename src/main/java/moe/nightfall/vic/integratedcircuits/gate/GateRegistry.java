package moe.nightfall.vic.integratedcircuits.gate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI.Type;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.client.SocketRenderer;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateRegistry implements IGateRegistry {
	private BiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private Map<Type, List<GateIOProvider>> ioProviderRegistry = Maps.newHashMap();

	public GateRegistry() {

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
		if (Loader.instance()
			.getModState(Loader.instance().getModObjectList().inverse().get(IntegratedCircuits.instance))
			.compareTo(ModState.INITIALIZED) > 0) {
			IntegratedCircuits.logger.fatal("Tried to register gate provider instance after initialization phase: "
					+ Loader.instance().activeModContainer());
		}
		List<GateIOProvider> list = ioProviderRegistry.get(element);
		if(list == null)
			list = new LinkedList<GateIOProvider>();
		list.add(provider);
		ioProviderRegistry.put(element, list);
	}

	public List<GateIOProvider> getIOProviderList(Class<?> clazz) {
		return ioProviderRegistry.get(clazz);
	}
}

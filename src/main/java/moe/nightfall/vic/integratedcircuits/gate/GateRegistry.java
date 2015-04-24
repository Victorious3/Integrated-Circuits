package moe.nightfall.vic.integratedcircuits.gate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateRegistry implements IGateRegistry {
	private HashBiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private HashMap<Class<?>, IPartRenderer<?>> rendererRegistry = Maps.newHashMap();
	private HashMap<Class<?>, List<GateIOProvider>> ioProviderRegistry = Maps.newHashMap();

	@Override
	public void registerGate(String name, Class<? extends IGate> clazz) {
		registry.put(name, clazz);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer) {
		rendererRegistry.put(clazz, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz) {
		return (IPartRenderer<IGate>) rendererRegistry.get(clazz);
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
	public void registerGateIOProvider(GateIOProvider provider, Class<?>... classes) {
		for (Class<?> clazz : classes)
			registerGateIOProvider(provider, clazz);
	}

	private void registerGateIOProvider(GateIOProvider provider, Class<?> clazz) {
		if (Loader.instance()
			.getModState(Loader.instance().getModObjectList().inverse().get(IntegratedCircuits.instance))
			.compareTo(ModState.INITIALIZED) > 0) {
			IntegratedCircuits.logger.fatal("Tried to register gate provider instance after initialization phase: "
					+ Loader.instance().activeModContainer());
		}
		List<GateIOProvider> list = ioProviderRegistry.getOrDefault(clazz, new LinkedList());
		list.add(provider);
		ioProviderRegistry.put(clazz, list);
	}

	public List<GateIOProvider> getIOProviderList(Class<?> clazz) {
		return ioProviderRegistry.get(clazz);
	}

	public <T> T createProxyInstance(Class<T> clazz) {
		try {
			Pair<ProxyFactory, ProxyCache> pair = createProxyFactory(clazz);
			ProxyFactory pf = pair.getLeft();
			Proxy proxy = (Proxy) pf.create(new Class[0], new Object[0]);
			proxy.setHandler(pair.getRight());
			ioProviderRegistry.put(proxy.getClass(), getIOProviderList(clazz));
			return (T) proxy;
		} catch (Exception e) {
			IntegratedCircuits.logger.fatal("Couldn't initialize proxy class for " + clazz);
			throw new RuntimeException(e);
		}
	}

	public <T> Class<T> createProxyClass(Class<T> clazz) {
		Pair<ProxyFactory, ProxyCache> pair = createProxyFactory(clazz);
		ProxyFactory pf = pair.getLeft();
		pf.setHandler(pair.getRight());
		Class<T> clazz2 = pf.createClass();
		ioProviderRegistry.put(clazz2, getIOProviderList(clazz));
		return clazz2;
	}

	private Pair<ProxyFactory, ProxyCache> createProxyFactory(Class<?> clazz) {
		try {
			IntegratedCircuits.logger.info("Creating proxy class for " + clazz);
			List<GateIOProvider> list = ioProviderRegistry.getOrDefault(clazz, new LinkedList());
			ProxyFactory pf = new ProxyFactory();
			pf.setSuperclass(clazz);
			HashSet<Class> proxyInterfaces = Sets.newHashSet();

			Iterator<GateIOProvider> iterator = list.iterator();
			while (iterator.hasNext()) {
				GateIOProvider provider = iterator.next();
				Annotation[] annotations = provider.getClass().getAnnotations();

				int numInterfaces = 0;
				for (Annotation annotation : annotations) {
					if (annotation instanceof Interface) {
						numInterfaces += addInterface((Interface) annotation, proxyInterfaces);
					} else if (annotation instanceof InterfaceList) {
						for (Interface intf : ((InterfaceList) annotation).value()) {
							numInterfaces += addInterface(intf, proxyInterfaces);
						}
					}
				}
				if (numInterfaces > 0) {
					IntegratedCircuits.logger.info("Added the following interfaces from GateIOProvider "
							+ provider.getClass());
					IntegratedCircuits.logger.info(proxyInterfaces);
				}
			}

			pf.setInterfaces(proxyInterfaces.toArray(new Class[proxyInterfaces.size()]));
			pf.setFilter(new MethodFilterImpl(proxyInterfaces));

			return new ImmutablePair<ProxyFactory, ProxyCache>(pf, new ProxyCache(list, proxyInterfaces));
		} catch (Exception e) {
			IntegratedCircuits.logger.fatal("Couldn't initialize proxy class for " + clazz);
			throw new RuntimeException(e);
		}
	}

	private int addInterface(Interface intf, Set<Class> proxyInterfaces) throws ClassNotFoundException {
		if (Loader.isModLoaded(intf.modid())) {
			Class intfClazz = Class.forName(intf.iface());
			proxyInterfaces.add(intfClazz);
			return 1;
		}
		return 0;
	}

	private class MethodFilterImpl implements MethodFilter {

		public Set<Method> interfaceMethods = Sets.newHashSet();

		public MethodFilterImpl(Set<Class> interfaces) {
			for (Class clazz : interfaces) {
				interfaceMethods.addAll(Arrays.asList(clazz.getMethods()));
			}
		}

		@Override
		public boolean isHandled(Method m) {
			return interfaceMethods.contains(m);
		}
	}

	private class ProxyCache implements MethodHandler {

		private HashMap<Method, GateIOProvider> map = Maps.newHashMap();

		public ProxyCache(List<GateIOProvider> providers, Set<Class> interfaces) {
			for (Class clazz : interfaces) {
				GateIOProvider provider = null;
				for (GateIOProvider p : providers) {
					if (clazz.isInstance(p)) {
						provider = p;
						break;
					}
				}
				if (provider == null)
					throw new RuntimeException();
				for (Method method : clazz.getMethods()) {
					map.put(method, provider);
				}
			}
		}

		@Override
		public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
			GateIOProvider provider = map.get(thisMethod);
			if (self instanceof ISocketWrapper) {
				provider.socket = ((ISocketWrapper) self).getSocket();
			}
			return thisMethod.invoke(provider, args);
		}
	}
}

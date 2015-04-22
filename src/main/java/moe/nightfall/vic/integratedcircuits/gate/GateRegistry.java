package moe.nightfall.vic.integratedcircuits.gate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.IGate;
import moe.nightfall.vic.integratedcircuits.api.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;
import moe.nightfall.vic.integratedcircuits.api.ISocketWrapper;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateRegistry implements IGateRegistry
{
	private HashBiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private HashMap<Class<?>, IPartRenderer<?>> rendererRegistry = Maps.newHashMap();
	private HashMap<Class<? extends ISocketWrapper>, List<GateIOProvider>> ioProviderRegistry = Maps.newHashMap();
	
	@Override
	public void registerGate(String name, Class<? extends IGate> clazz)
	{
		registry.put(name, clazz);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer)
	{
		rendererRegistry.put(clazz, renderer);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz) 
	{
		return (IPartRenderer<IGate>) rendererRegistry.get(clazz);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(String gateID) 
	{
		return getRenderer(registry.get(gateID));
	}
	
	@Override
	public String getName(Class<? extends IGate> gate)
	{
		return registry.inverse().get(gate);
	}
	
	@Override
	public IGate createGateInstace(String name)
	{
		try {
			return registry.get(name).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Coundn't instance gate \"" + name + "\", need an empty constructor!");
		}
	}

	@Override
	public void registerGateIOProvider(GateIOProvider provider, Class<? extends ISocketWrapper> clazz)
	{
		if(Loader.instance().getModState(Loader.instance().getModObjectList().inverse().get(IntegratedCircuits.instance)).compareTo(ModState.INITIALIZED) > 0) {
			IntegratedCircuits.logger.fatal("Tried to register gate provider instance after initialization phase: " + Loader.instance().activeModContainer());
		}
		List<GateIOProvider> list = ioProviderRegistry.getOrDefault(clazz, new LinkedList());
		list.add(provider);
		ioProviderRegistry.put(clazz, list);
	}
	
	public <T extends ISocketWrapper> Class<T> createProxyClass(Class<T> clazz) {
		try {
    		List<GateIOProvider> list = ioProviderRegistry.getOrDefault(clazz, new LinkedList());
    		ProxyFactory pf = new ProxyFactory();
    		pf.setSuperclass(clazz);
    		List<Class> proxyInterfaces = Lists.newArrayList();
    		
    		for(GateIOProvider provider : list) {
    			Annotation[] annotations = provider.getClass().getAnnotations();
    			
    			for(Annotation annotation : annotations) {
    				if(annotation instanceof Interface) {
    					addInterface((Interface) annotation, proxyInterfaces);
    				} else if(annotation instanceof InterfaceList) {
    					for(Interface intf : ((InterfaceList)annotation).value()) {
    						addInterface(intf, proxyInterfaces);
    					}
    				}
    			}
    		}
    		
    		pf.setInterfaces(proxyInterfaces.toArray(new Class[proxyInterfaces.size()]));
    		pf.setFilter(new MethodFilterImpl(proxyInterfaces));
    		pf.setHandler(new MethodHandlerImpl(list, proxyInterfaces));
    		
    		return pf.createClass();
		} catch (Exception e) {
			IntegratedCircuits.logger.fatal("Couldn't initialize proxy class for " + clazz);
			throw new RuntimeException(e);
		}
	}
	
	private class MethodFilterImpl implements MethodFilter {
		
		public HashSet<Method> interfaceMethods = Sets.newHashSet();
		
		public MethodFilterImpl(List<Class> interfaces) {
			for(Class clazz : interfaces) {
				interfaceMethods.addAll(Arrays.asList(clazz.getMethods()));
			}
		}
		
		@Override
		public boolean isHandled(Method m) {
			return interfaceMethods.contains(m);
		}
	}
	
	private class MethodHandlerImpl implements MethodHandler {
		
		private MethodCache cache;
		
		public MethodHandlerImpl(List<GateIOProvider> provider, List<Class> interfaces) {
			cache = new MethodCache(provider, interfaces);
		}
		
		@Override
		public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
			return cache.invoke(thisMethod, self, args);
		}
	}
	
	private void addInterface(Interface intf, List<Class> proxyInterfaces) throws ClassNotFoundException {
		if(Loader.isModLoaded(intf.modid())) {
			Class intfClazz = Class.forName(intf.iface());
			proxyInterfaces.add(intfClazz);
		}
	}
	
	private class MethodCache {
		
		private HashMap<Method, GateIOProvider> map = Maps.newHashMap();
		
		public MethodCache(List<GateIOProvider> providers, List<Class> interfaces) {
			for(Class clazz : interfaces) {
				GateIOProvider provider = null;
				for(GateIOProvider p : providers) {
					if(clazz.isInstance(p)) {
						provider = p;
						break;
					}
				}
				if(provider == null) throw new RuntimeException();
				for(Method method : clazz.getMethods()) {
					map.put(method, provider);
				}
			}
		}
		
		public Object invoke(Method method, Object self, Object[] args) throws Exception {
			GateIOProvider provider = map.get(method);
			provider.socket = ((ISocketWrapper)self).getSocket();
			return method.invoke(provider, args);
		}
	}
}

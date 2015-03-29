package vic.mod.integratedcircuits.gate;

import java.util.HashMap;

import com.google.common.collect.Maps;

public class GateRegistry
{
	private static HashMap<String, IGate> registry = Maps.newHashMap();
	
	private GateRegistry() {}
	
	public static void registerGate(IGate gate)
	{
		registry.put(gate.getName(), gate);
	}
	
	public static IGate createGateInstace(String name)
	{
		return registry.get(name).newInstance();
	}
}

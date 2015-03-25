package vic.mod.integratedcircuits.gate;

import java.util.HashMap;

import com.google.common.collect.Maps;

public class GateRegistry
{
	private static HashMap<String, PartGate> registry = Maps.newHashMap();
	
	private GateRegistry() {}
	
	public static void registerGate(PartGate gate)
	{
		registry.put(gate.getName(), gate);
	}
	
	public static PartGate createGateInstace(String name)
	{
		return registry.get(name).newInstance();
	}
}

package vic.mod.integratedcircuits.gate;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.item.ItemPartGate;

import com.google.common.collect.Maps;

public class GateRegistry
{
	private static HashMap<String, PartGate> registry = Maps.newHashMap();
	
	private GateRegistry() {}
	
	public static GateRegistry.ItemGatePair registerGate(PartGate gate, Class<? extends ItemPartGate> clazz)
	{
		registry.put(gate.getName(), gate);
		return new GateRegistry.ItemGatePair(gate, clazz);
	}
	
	public static PartGate createGateInstace(String name)
	{
		return registry.get(name).newInstance();
	}
	
	public static class ItemGatePair
	{
		private ItemPartGate item;
		private ItemPartGate itemFMP;
		
		private ItemGatePair(PartGate gate, Class<? extends ItemPartGate> clazz)
		{
			try {
				Constructor<? extends ItemPartGate> constr = clazz.getConstructor(String.class, PartGate.class, boolean.class);
				item = constr.newInstance(gate.getName(), gate, false);
				if(IntegratedCircuits.isFMPLoaded)
					itemFMP = constr.newInstance(gate.getName(), gate, true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public ItemPartGate getItem()
		{
			return item;
		}
		
		public ItemPartGate getItemFMP()
		{
			return itemFMP;
		}
	}

}

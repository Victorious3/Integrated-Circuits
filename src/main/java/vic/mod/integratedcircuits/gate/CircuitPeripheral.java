package vic.mod.integratedcircuits.gate;

import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IProperty;
import vic.mod.integratedcircuits.misc.Vec2;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class CircuitPeripheral extends GatePeripheral
{
	private static MethodProvider methodProvider = new MethodProvider()
		.registerMethod("getGateAt", Double.class, Double.class)
		.registerMethod("getPowerTo", Double.class, Double.class)
		.registerMethod("getGateProperties", Double.class, Double.class)
		.registerMethod("getGateProperty", Double.class, Double.class, String.class)
		.registerMethod("setGateProperty", Double.class, Double.class, String.class, Object.class)
		.registerMethod("getOutputToSide", Double.class)
		.registerMethod("getInputFromSide", Double.class)
		.registerMethod("getGateName", Double.class)
		.registerMethod("getSize")
		.registerMethod("getName")
		.registerMethod("getAuthor");
	
	private PartCircuit circuit;
	
	public CircuitPeripheral(PartCircuit circuit)
	{
		this.circuit = circuit;
	}
	
	@Override
	public String getType() 
	{
		return "IC Circuit";
	}

	@Override
	public Object[] callMethod(Method method, IComputerAccess computer, ILuaContext context, Object[] arguments) throws LuaException, InterruptedException 
	{
		CircuitData cdata = circuit.getCircuitData();
		synchronized(cdata) 
		{
			if(method.getName().equals("getSize"))
				return new Object[]{cdata.getSize()};
			else if(method.getName().equals("getName"))
				return new Object[]{cdata.getProperties().getName()};
			else if(method.getName().equals("getAuthor"))
				return new Object[]{cdata.getProperties().getAuthor()};
			else if(method.getName().equals("getOutputToSide") || method.getName().equals("getInputFromSide"))
			{
				int side = ((Double)arguments[1]).intValue();
				if(side < 0 || side > 3) throw new LuaException(String.format("Illegal side provided. (%s) [0->3]", side));
				
				byte[] value = circuit.output[side];
				if(method.getName().equals("getOutputToSide"))
					value = circuit.output[side];
				else value = circuit.input[side];
				
				Object[] ret = new Object[value.length];
				for(int i = 0; i < value.length; i++)
					ret[i] = Integer.valueOf(value[i]);
				return ret;
			}
			else if(method.getName().equals("getGateName"))
			{
				int id = ((Double)arguments[1]).intValue();
				CircuitPart cp = CircuitPart.getPart(id);
				return new Object[]{cp};
			}
			else
			{
				int x = ((Double)arguments[1]).intValue();
				int y = ((Double)arguments[2]).intValue();
				
				Vec2 pos = new Vec2(x, y);
				int size = cdata.getSize();
				if(x < 0 || y < 0 || x >= size || y >= size) 
					throw new LuaException(String.format("Supplied position out of bounds (%s|%s) [0->%s]", x, y, size - 1));
				CircuitPart cp = cdata.getPart(pos);
				
				if(method.getName().equals("getGateAt"))
				{						
					String name = cp.getName(pos, circuit);
					int id = CircuitPart.getId(cp);
					int meta = cp.getState(pos, circuit);
					return new Object[]{name, id, meta};
				}
				else if(method.getName().equals("getPowerTo"))
				{
					boolean b1 = cp.getInputFromSide(pos, circuit, ForgeDirection.NORTH);
					boolean b2 = cp.getInputFromSide(pos, circuit, ForgeDirection.EAST);
					boolean b3 = cp.getInputFromSide(pos, circuit, ForgeDirection.SOUTH);
					boolean b4 = cp.getInputFromSide(pos, circuit, ForgeDirection.WEST);
					
					return new Object[]{b1, b2, b3, b4};
				}
				else if(method.getName().equals("getGateProperties"))
				{
					Map<String, IProperty> properties = cp.stitcher.getProperties();
					return properties.keySet().toArray();
				}
				else if(method.getName().contains("GateProperty"))
				{
					int state = cp.getState(pos, circuit);
					IProperty property = cp.stitcher.getPropertyByName((String)arguments[3]);
					if(property == null) throw new LuaException(String.format("No property by the name of '&s' found for gate %s", arguments[3], cp.getName(pos, circuit)));
					if(method.getName().contains("get"))
						return new Object[]{property.get(state), property.getName(), property.getClass().getSimpleName()};
					else
					{
						try {
							cp.setProperty(pos, circuit, property, (Comparable)arguments[4]);
						} catch (Exception e) {
							throw new LuaException(e.getMessage());
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) 
	{
		computer.mount("rom/programs/" + Constants.MOD_ID, new FileMount("lua"));
	}

	@Override
	public MethodProvider getMethodProvider() 
	{
		return methodProvider;
	}
}

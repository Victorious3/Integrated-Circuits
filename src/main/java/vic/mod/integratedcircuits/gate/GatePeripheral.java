package vic.mod.integratedcircuits.gate;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class GatePeripheral implements IPeripheral
{
	@Override
	public final String[] getMethodNames() 
	{
		MethodProvider provider = getMethodProvider();
		String[] ret = new String[provider.methods.size()];
		for(int i = 0; i < provider.methods.size(); i++)
			ret[i] = provider.methods.get(i).getName();
		return ret;
	}

	@Override
	public final Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException 
	{
		MethodProvider provider = getMethodProvider();
		Method m = provider.methods.get(method);
		m.match(arguments);
		return callMethod(m, computer, context, arguments);
	}
	
	public abstract Object[] callMethod(Method method, IComputerAccess computer, ILuaContext context, Object[] arguments) throws LuaException, InterruptedException;

	public abstract MethodProvider getMethodProvider();
	
	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public void attach(IComputerAccess computer) {}
	
	@Override
	public boolean equals(IPeripheral other) 
	{
		return other.getType().equals(getType());
	}
	
	public static class Method
	{
		private String name;
		private Class[] parameters;
		
		private Method(String name, Class[] parameters)
		{
			this.name = name;
			this.parameters = parameters;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void match(Object[] args) throws LuaException
		{
			if(args.length - 1 != parameters.length) throw new LuaException("Illegal amount of parameters!");
			for(int i = 0; i < parameters.length; i++)
			{
				if(!parameters[i].isAssignableFrom(args[i + 1].getClass())) 
					throw new LuaException("Illegal parameter at index " + i + ". Expected '" + parameters[i] + "', got '" + args[i + 1].getClass() + "'.");
			}
		}
	}
	
	public static class MethodProvider
	{
		private ArrayList<Method> methods = Lists.newArrayList();
		
		public MethodProvider registerMethod(String name, Class... parameters)
		{
			methods.add(new Method(name, parameters));
			return this;
		}
	}
}

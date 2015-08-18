package moe.nightfall.vic.integratedcircuits.gate.peripheral;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public abstract class GatePeripheral implements IPeripheral {

	private final Map<String, Method> methods = Maps.newLinkedHashMap();
	private final String[] methodNames;

	public GatePeripheral() {
		for (Method m : getClass().getMethods()) {
			if (m.getAnnotation(LuaMethod.class) != null) {
				methods.put(m.getName(), m);
			}
		}
		methodNames = methods.keySet().toArray(new String[methods.size()]);
	}

	@Override
	public final String[] getMethodNames() {
		return methodNames;
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public final Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
		String name = (String) methods.keySet().toArray()[method];
		return callMethod(name, arguments);
	}

	public final Object[] callMethod(String name, Object[] arguments) throws LuaException, InterruptedException {
		try {
			Method m = methods.get(name);
			if (m == null) {
				return null;
			}
			Class<?>[] paramTypes = m.getParameterTypes();
			if (arguments.length != paramTypes.length) {
				throw new LuaException("Illegal amount of parameters!");
			}
			for (int i = 0; i < paramTypes.length; i++) {
				paramTypes[i] = Primitives.wrap(paramTypes[i]);
				if (!paramTypes[i].isAssignableFrom(arguments[i].getClass()))
					throw new LuaException("Illegal parameter at index " + i + ". Expected '" + paramTypes[i] + "', got '" + arguments[i].getClass() + "'.");
			}
			Object o = m.invoke(this, arguments);
			if (o == null) {
				return null;
			} else if (m.getReturnType().isArray()) {
				return (Object[]) o;
			} else {
				return new Object[] { o };
			}
		} catch (LuaException e) {
			throw e;
		} catch (Exception e) {
			throw new LuaException(e.getMessage());
		}
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other) {
		return other.getType().equals(getType());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface LuaMethod {

	}
}

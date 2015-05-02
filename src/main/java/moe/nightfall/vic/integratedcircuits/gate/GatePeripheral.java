package moe.nightfall.vic.integratedcircuits.gate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.Optional.Interface;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

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
			if (arguments.length != m.getParameterCount()) {
				throw new LuaException("Illegal amount of parameters!");
			}
			for (int i = 0; i < m.getParameterTypes().length; i++) {
				if (!m.getParameterTypes()[i].isAssignableFrom(arguments[i].getClass()))
					throw new LuaException("Illegal parameter at index " + i + ". Expected '" + m.getParameterTypes()[i] + "', got '" + arguments[i].getClass() + "'.");
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
	public void detach(IComputerAccess computer) {
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other.getType().equals(getType());
	}

	public static @interface LuaMethod {

	}

	@Interface(iface = "dan200.computercraft.api.filesystem.IMount", modid = "ComputerCraft")
	public static class FileMount implements IMount {
		private ArrayList<String> files;
		private String path;

		public FileMount(String path) {
			try {
				this.path = "assets/" + Constants.MOD_ID + "/" + path + "/";
				files = new ArrayList<String>();

				String jfpath = IntegratedCircuits.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				if (jfpath.contains("!")) {
					jfpath = jfpath.substring(0, jfpath.indexOf("!"));
					JarFile jar = new JarFile(new File(new URL(jfpath).toURI()));
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						String name = entries.nextElement().getName();
						if (!name.equals(this.path) && name.startsWith(this.path))
							files.add(name.substring(this.path.length()));
					}
					jar.close();
				} else {
					URL url = IntegratedCircuits.class.getResource("/" + this.path);
					if (url != null) {
						File apps = new File(url.toURI());
						for (File app : apps.listFiles())
							files.add(app.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean exists(String path) throws IOException {
			return path.equals("") || files.contains(path);
		}

		@Override
		public boolean isDirectory(String path) throws IOException {
			return path.equals("");
		}

		@Override
		public void list(String path, List<String> contents) throws IOException {
			contents.addAll(files);
		}

		@Override
		public long getSize(String path) throws IOException {
			return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path).available();
		}

		@Override
		public InputStream openForRead(String path) throws IOException {
			if (!exists(path))
				throw new IOException();
			return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path);
		}
	}
}

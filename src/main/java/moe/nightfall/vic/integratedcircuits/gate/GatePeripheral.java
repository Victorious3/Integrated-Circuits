package moe.nightfall.vic.integratedcircuits.gate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;

import com.google.common.collect.Lists;

import dan200.computercraft.api.filesystem.IMount;
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
		return callMethod(m, arguments);
	}
	
	public final Object[] callMethod(String method, Object[] arguments) throws LuaException, InterruptedException 
	{
		MethodProvider provider = getMethodProvider();
		for(int i = 0; i < provider.methods.size(); i++) {
			Method m = provider.methods.get(i);
			if(m.name.equals(method)) {
				return callMethod(m, arguments);
			}
		}
		return null;
	}
	
	public abstract Object[] callMethod(Method method, Object[] arguments) throws LuaException, InterruptedException;
	
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
			if(args.length != parameters.length) throw new LuaException("Illegal amount of parameters!");
			for(int i = 0; i < parameters.length; i++)
			{
				if(!parameters[i].isAssignableFrom(args[i].getClass())) 
					throw new LuaException("Illegal parameter at index " + i + ". Expected '" + parameters[i] + "', got '" + args[i].getClass() + "'.");
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
	
	public static class FileMount implements IMount
	{
		private ArrayList<String> files;
		private String path;
		
		public FileMount(String path)
		{
			try {
				this.path = "assets/" + Constants.MOD_ID + "/" + path + "/";
				files = new ArrayList<String>();
				
				String jfpath = IntegratedCircuits.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				if(jfpath.contains("!")) {
					jfpath = jfpath.substring(0, jfpath.indexOf("!"));
					JarFile jar = new JarFile(new File(new URL(jfpath).toURI()));
					Enumeration<JarEntry> entries = jar.entries();
					while(entries.hasMoreElements()) 
					{
						String name = entries.nextElement().getName();
						if(!name.equals(this.path) && name.startsWith(this.path)) files.add(name.substring(this.path.length()));
					}
					jar.close();
				}
				else
				{
					URL url = IntegratedCircuits.class.getResource("/" + this.path);
					if (url != null) 
					{
						File apps = new File(url.toURI());
						for (File app : apps.listFiles()) files.add(app.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public boolean exists(String path) throws IOException 
		{
			return path.equals("") || files.contains(path);
		}

		@Override
		public boolean isDirectory(String path) throws IOException 
		{
			return path.equals("");
		}

		@Override
		public void list(String path, List<String> contents) throws IOException 
		{
			contents.addAll(files);
		}

		@Override
		public long getSize(String path) throws IOException
		{
			return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path).available();
		}

		@Override
		public InputStream openForRead(String path) throws IOException 
		{
			if(!exists(path)) throw new IOException();
			return IntegratedCircuits.class.getResourceAsStream("/" + this.path + path);
		}
	}
}

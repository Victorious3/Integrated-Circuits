package vic.mod.integratedcircuits.part;

import java.util.HashMap;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

public class PartFactory implements IPartFactory
{
	private static HashMap<String, GatePart> parts = new HashMap<String, GatePart>();
	private static PartFactory instance = new PartFactory();
	
	private PartFactory() {}
	
	public static void register(GatePart part)
	{
		parts.put(part.getType(), part);
	}
	
	@Override
	public TMultiPart createPart(String arg0, boolean arg1) 
	{
		GatePart part = parts.get(arg0);
		if(part == null) return null;
		return part.newInstance();
	}
	
	public static void initialize()
	{
		String[] keys = parts.keySet().toArray(new String[parts.keySet().size()]);
		MultiPartRegistry.registerParts(instance, keys);
	}
}

package vic.mod.integratedcircuits.gate.fmp;

import java.util.HashMap;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

//TODO Use the GateRegistry instead, not sure if I'll ever create a part that is NOT a gate.
public class PartFactory implements IPartFactory
{
	private static HashMap<String, FMPartGate> parts = new HashMap<String, FMPartGate>();
	private static PartFactory instance = new PartFactory();
	
	private PartFactory() {}
	
	public static void register(FMPartGate fmPartGate)
	{
		parts.put(fmPartGate.getType(), fmPartGate);
	}
	
	@Override
	public TMultiPart createPart(String arg0, boolean arg1) 
	{
		FMPartGate part = parts.get(arg0);
		if(part == null) return null;
		return part.newInstance();
	}
	
	public static void initialize()
	{
		String[] keys = parts.keySet().toArray(new String[parts.keySet().size()]);
		MultiPartRegistry.registerParts(instance, keys);
	}
}

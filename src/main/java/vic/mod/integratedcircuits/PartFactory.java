package vic.mod.integratedcircuits;

import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

public class PartFactory implements IPartFactory
{
	@Override
	public TMultiPart createPart(String arg0, boolean arg1) 
	{
		if(arg0.equals(IntegratedCircuits.partCircuit)) return new PartCircuit();
		return null;
	}
}

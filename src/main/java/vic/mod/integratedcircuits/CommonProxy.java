package vic.mod.integratedcircuits;

import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy 
{
	public void initialize()
	{
		registerNetwork();
	}
	
	public void registerNetwork()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(IntegratedCircuits.instance, new GuiHandler());
	}
}

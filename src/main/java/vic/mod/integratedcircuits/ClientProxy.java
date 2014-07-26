package vic.mod.integratedcircuits;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void initialize() 
	{
		super.initialize();
		registerRenderers();
	}
	
	public void registerRenderers()
	{
		
	}
}

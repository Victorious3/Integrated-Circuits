package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TileEntitySemiTransparentRenderer extends TileEntitySpecialRenderer
{
	protected int renderPass;
	
	public int getCurrentRenderPass()
	{
		return renderPass;
	}
	
	protected void addToRenderQueue(int x, int y, int z)
	{
		ClientProxy.stRenderer.addToRenderQueue(x, y, z);
	}
}

package vic.mod.integratedcircuits.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import vic.mod.integratedcircuits.TileEntityAssembler;

public class TileEntityAssemblerRenderer extends TileEntitySpecialRenderer
{
	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks)
	{
		
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks)
	{
		this.renderTileEntityAt((TileEntityAssembler)te, x, y, z, partialTicks);
	}
}

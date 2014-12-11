package vic.mod.integratedcircuits.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import vic.mod.integratedcircuits.DiskDrive;
import vic.mod.integratedcircuits.DiskDrive.ModelFloppy;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;

public class TileEntityPCBLayoutRenderer extends TileEntitySpecialRenderer
{
	//TODO Might want to get these magic numbers from the floppy's AABB.
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	public void renderTileEntityAt(TileEntityPCBLayout te, double x, double y, double z, float partialTicks) 
	{
		DiskDrive.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) 
	{
		renderTileEntityAt((TileEntityPCBLayout)te, x, y, z, partialTicks);
	}
}

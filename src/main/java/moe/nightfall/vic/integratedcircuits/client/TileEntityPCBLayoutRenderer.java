package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.DiskDrive;
import moe.nightfall.vic.integratedcircuits.DiskDrive.ModelFloppy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPCBLayout;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPCBLayoutRenderer extends TileEntitySpecialRenderer {
	// TODO Might want to get these magic numbers from the floppy's AABB.
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);

	public void renderTileEntityAt(TileEntityPCBLayout te, double x, double y, double z, float partialTicks) {
		DiskDrive.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		renderTileEntityAt((TileEntityPCBLayout) te, x, y, z, partialTicks);
	}
}

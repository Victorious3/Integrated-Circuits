package moe.nightfall.vic.integratedcircuits.client;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Quat;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Translation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPrinter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityPrinterRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		renderWorldBlock(null, 0, 0, 0, block, modelId, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int rotation = 0;
		if (world != null) {
			TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
			if (te != null) rotation = te.rotation;
		}
		
		CCRenderState.reset();
		
		if (world != null) {
			// TODO Doesn't work.
			CCRenderState.lightMatrix.locate(world, x, y, z);
			CCRenderState.setBrightness(world, x, y, z);
		}
		
		TransformationList tls = new Rotation(rotation * 90, 0, 0, 1).with(new Translation(x, y, z));
		
		CCModel.quadModel(16).generateBlock(0, 0, 0, 0, 1, 0.5, 1, 2 | 1).computeNormals()
			.render(tls, new IconTransformation(Resources.ICON_ASSEMBLER_SIDE));
		CCModel.quadModel(4).generateBlock(0, 0, 0, 0, 1, 0.5, 1, ~1).computeNormals()
			.render(tls, new IconTransformation(Resources.ICON_ASSEMBLER_BOTTOM));
		CCModel.quadModel(4).generateBlock(0, 0, 0, 0, 1, 0.5, 1, ~2).computeNormals()
			.render(tls, new IconTransformation(Resources.ICON_PRINTER_TOP));
		
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return Constants.PRINTER_RENDER_ID;
	}

}

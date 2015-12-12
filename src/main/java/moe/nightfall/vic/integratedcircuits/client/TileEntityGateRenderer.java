package moe.nightfall.vic.integratedcircuits.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;

public class TileEntityGateRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
			RenderBlocks renderer) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(x, y, z);
		if (te == null || te.getSocket() == null)
			return false;

		CCRenderState.reset();
		CCRenderState.lightMatrix.locate(world, x, y, z);
		CCRenderState.setBrightness(world, x, y, z);

		ClientProxy.socketRenderer.prepare(te.getSocket());
		ClientProxy.socketRenderer.renderStatic(new Translation(new Vector3(x, y, z)));

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return Constants.GATE_RENDER_ID;
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float par5) {
		ISocket socket = ((TileEntitySocket) te).getSocket();
		if (te == null || (socket == null))
			return;

		CCRenderState.reset();
		CCRenderState.pullLightmap();
		CCRenderState.setDynamic();
		TextureUtils.bindAtlas(0);

		ClientProxy.socketRenderer.prepareDynamic(socket, par5);
		ClientProxy.socketRenderer.renderDynamic(new Translation(new Vector3(x, y, z)));
	}
}

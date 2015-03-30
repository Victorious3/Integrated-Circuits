package vic.mod.integratedcircuits.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.gate.Socket;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class TileEntityGateRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler
{
	@Override 
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		
		CCRenderState.reset();
		CCRenderState.lightMatrix.locate(world, x, y, z);
		
		ClientProxy.socketRenderer.prepare(te.getSocket());
		ClientProxy.socketRenderer.renderStatic(new Translation(new Vector3(x, y, z)), te.getSocket().getOrientation());
		
		return true;
	} 

	@Override
	public boolean shouldRender3DInInventory(int modelId) 
	{
		return false;
	}

	@Override
	public int getRenderId() 
	{
		return Constants.GATE_RENDER_ID;
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float par5) 
	{
		Socket socket = ((TileEntityGate)te).getSocket();
		CCRenderState.reset();
		CCRenderState.pullLightmap();
		CCRenderState.useNormals = true;
		TextureUtils.bindAtlas(0);
		
		ClientProxy.socketRenderer.prepareDynamic(socket, par5);
		ClientProxy.socketRenderer.renderDynamic(socket.getRotationTransformation().with(new Translation(new Vector3(x, y, z))));
	}
}

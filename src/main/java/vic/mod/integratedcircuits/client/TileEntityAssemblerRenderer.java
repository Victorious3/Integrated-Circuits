package vic.mod.integratedcircuits.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.DiskDriveUtils.ModelFloppy;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;

public class TileEntityAssemblerRenderer extends TileEntitySpecialRenderer
{
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	private ResourceLocation safetyRegulationsTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_safety.png");
	private ResourceLocation bottomTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_bottom.png");
	
	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks)
	{	
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		Tessellator tes = Tessellator.instance;
		this.bindTexture(bottomTex);
		tes.startDrawingQuads();
		tes.addVertexWithUV(0, 8 / 16F, 0, 0, 0);
		tes.addVertexWithUV(0, 8 / 16F, 1, 0, 1);
		tes.addVertexWithUV(1, 8 / 16F, 1, 1, 1);
		tes.addVertexWithUV(1, 8 / 16F, 0, 1, 0);
		tes.draw();
		
		if(te.circuitFBO != null)
		{
			te.circuitFBO.bindFramebufferTexture();
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, 9 / 16F, 0, 0, 0);
			tes.addVertexWithUV(0, 9 / 16F, 1, 0, 1);
			tes.addVertexWithUV(1, 9 / 16F, 1, 1, 1);
			tes.addVertexWithUV(1, 9 / 16F, 0, 1, 0);
			tes.draw();
		}

		GL11.glRotatef(180, 0, 0, 1);
		GL11.glTranslatef(-1.005F, -1, 0);
		
		this.bindTexture(safetyRegulationsTex);
		tes.startDrawingQuads();
		tes.addVertexWithUV(0, 9 / 16F, 1 - 7 / 16F, 0, 0);
		tes.addVertexWithUV(0, 1 - 3 / 16F, 1 - 7 / 16F, 0, 1);
		tes.addVertexWithUV(0, 1 - 3 / 16F, 0 + 1 / 16F, 1, 1);
		tes.addVertexWithUV(0, 9 / 16F, 0 + 1 / 16F, 1, 0);
		tes.draw();
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks)
	{
		this.renderTileEntityAt((TileEntityAssembler)te, x, y, z, partialTicks);
	}
}

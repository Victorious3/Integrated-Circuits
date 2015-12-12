package moe.nightfall.vic.integratedcircuits.client;

import org.lwjgl.opengl.GL11;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPrinter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class TileEntityPrinterRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		TileEntityPrinter tep = (TileEntityPrinter) te;
		if (tep == null)
			return;

		float paperWidth = 1.5F / 16F * (tep.paperCount() / 16F);

		GL11.glPushMatrix();
		new Rotation((-tep.rotation + 2) * Math.PI / 2, 0, 1, 0).at(Vector3.center).with(new Translation(x, y, z)).glApply();
		
		GL11.glPushMatrix();
		GL11.glTranslatef(3.5F / 16F, 8 / 16F + 0.005F, 5 / 16F);
		GL11.glRotatef(90, 1, 0, 0);
		GL11.glScalef(1 / 156F, 1 / 156F, 1 / 156F);

		GL11.glDisable(GL11.GL_LIGHTING);
		RenderUtils.setBrightness(240, 240);

		int inkLevel = (int) (tep.inkLevel() * 100);
		int paperCount = tep.paperCount();
		
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		fr.drawSplitString(
			// Realistic ink level warning at 50%
			(inkLevel <= 15 ? EnumChatFormatting.RED : inkLevel <= 50 ? EnumChatFormatting.YELLOW : "") + "I " + inkLevel + "%\n" + 
			(paperCount == 0 ? EnumChatFormatting.RED : paperCount < 3 ? EnumChatFormatting.YELLOW : EnumChatFormatting.WHITE) + "P " + paperCount + "/16", 0, 0, Integer.MAX_VALUE, 0xFFFFFF);
		
		GL11.glColor3f(1, 1, 1);
		RenderUtils.resetBrightness();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
		
		if (paperWidth > 0) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.5F / 16F, 4 / 16F, paperWidth + 1.25F / 16F);

			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
			IIcon paperIcon = Items.paper.getIconFromDamage(0);
			ItemRenderer.renderItemIn2D(Tessellator.instance, paperIcon.getMinU(), paperIcon.getMinV(),
					paperIcon.getMaxU(), paperIcon.getMaxV(), paperIcon.getIconWidth(), paperIcon.getIconHeight(),
					paperWidth);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glDisable(GL11.GL_LIGHTING);
		CCRenderState.startDrawing();
		renderWorldBlock(null, 0, 0, 0, block, modelId, renderer);
		CCRenderState.draw();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int rotation = 2;
		if (world != null) {
			TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(x, y, z);
			if (te != null) rotation = te.rotation;
		}
		
		CCRenderState.reset();
		
		if (world != null) {
			CCRenderState.lightMatrix.locate(world, x, y, z);
			CCRenderState.setBrightness(world, x, y, z);
		}
		
		TransformationList tls = new Rotation((-rotation + 2) * Math.PI / 2, 0, 1, 0).at(Vector3.center)
				.with(new Translation(x, y, z));
		
		CCModel.quadModel(16).generateBlock(0, 0, 0, 0, 1, 0.5, 1, 2 | 1).computeNormals()
			.apply(new IconTransformation(Resources.ICON_ASSEMBLER_SIDE)).computeLighting(LightModel.standardLightModel).render(tls);
		CCModel.quadModel(4).generateBlock(0, 0, 0, 0, 1, 0.5, 1, ~1).computeNormals()
			.apply(new IconTransformation(Resources.ICON_ASSEMBLER_BOTTOM)).computeLighting(LightModel.standardLightModel).render(tls);
		CCModel.quadModel(4).generateBlock(0, 0, 0, 0, 1, 0.5, 1, ~2).computeNormals()
			.apply(new IconTransformation(Resources.ICON_PRINTER_TOP)).computeLighting(LightModel.standardLightModel).render(tls);
		CCModel.quadModel(24).generateBlock(0, 3 / 16D, 0.5, 1 / 16D, 13 / 16D, 9 / 16D, 3 / 16D).computeNormals()
			.apply(new IconTransformation(Resources.ICON_IC_SOCKET)).computeLighting(LightModel.standardLightModel).render(tls);
		
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

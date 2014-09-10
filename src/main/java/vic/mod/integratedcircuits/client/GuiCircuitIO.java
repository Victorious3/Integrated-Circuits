package vic.mod.integratedcircuits.client;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.RenderUtils;

public class GuiCircuitIO extends GuiButton
{
	public int side;
	public int color;
	public boolean isActive;
	
	public GuiCircuitIO(int id, int x, int y, int color, int side) 
	{
		super(id, x, y, 8, 8, "");
		this.color = color;
		this.side = side;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) 
	{
		mc.getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(this.xPosition, this.yPosition, 0);
		GL11.glScalef(0.5F, 0.5F, 1F);
		RenderUtils.applyColorIRGB(MapColor.getMapColorForBlockColored(color).colorValue);
		drawTexturedModalRect(0, 0, 2 * 16, (getHoverState(field_146123_n) == 1 ? 15 : 14) * 16, 16, 16);
		GL11.glPopMatrix();
	}
}

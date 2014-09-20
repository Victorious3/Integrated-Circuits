package vic.mod.integratedcircuits.client;

import java.util.ArrayList;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.util.MiscUtils;
import vic.mod.integratedcircuits.util.RenderUtils;

public class GuiIO extends GuiButton implements IHoverable
{
	public int side;
	public int color;
	private GuiPCBLayout parent;
	private TileEntityPCBLayout te;
	private boolean isActive;
	
	public GuiIO(int id, int x, int y, int color, int side, GuiPCBLayout parent, TileEntityPCBLayout te) 
	{
		super(id, x, y, 9, 9, "");
		this.color = color;
		this.side = side;
		this.parent = parent;
		this.te = te;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) 
	{
		mc.getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
		this.field_146123_n = !parent.blockMouseInput && field_146123_n;
		if(getHoverState(field_146123_n) == 2) parent.hoveredElement = this;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(this.xPosition, this.yPosition, 0);
		GL11.glTranslatef(4F, 4F, 0F);
		GL11.glRotatef(side * 90, 0F, 0F, 1F);
		GL11.glTranslatef(-4F, -4F, -0F);
		
		ForgeDirection dir = MiscUtils.getDirection(side);
		isActive = (te.con >> side & 1) != 0 || color == 0;
		boolean isPowered = isActive && te.getInputFromSide(dir, color) || te.getOutputToSide(dir, color);
		
		if(isActive)
		{
			if(isPowered) GL11.glColor3f(0F, 1F, 0F);
			else GL11.glColor3f(0F, 0.4F, 0F);
			drawTexturedModalRect(0, 3, 5 * 8, 31 * 8, 8, 8);
		}

		GL11.glColor3f(0F, 0F, 0F);
		if(isActive) RenderUtils.applyColorIRGB(MapColor.getMapColorForBlockColored(color).colorValue);
		drawTexturedModalRect(0, 0, 4 * 8, (getHoverState(field_146123_n) == 2 || isPowered ? 30 : 31) * 8, 8, 8);
		
		if(isPowered) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexturedModalRect(0, 0, 5 * 8, 30 * 8, 8, 8);
		GL11.glPopMatrix();
		
		if(getHoverState(field_146123_n) == 2)
		{
			ArrayList<String> text = new ArrayList<String>();
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int par1, int par2) 
	{
		boolean b = super.mousePressed(mc, par1, par2) && !parent.blockMouseInput;
		if(b)
		{
			ForgeDirection dir = MiscUtils.getDirection(side);
			te.setInputFromSide(dir, color, !te.getInputFromSide(dir, color));
		}
		return b;
	}

	@Override
	public ArrayList<String> getHoverInformation() 
	{
		ArrayList<String> text = new ArrayList<String>();
		ForgeDirection dir = MiscUtils.getDirection(side);
		text.add("F: 0x" + Integer.toHexString(color));
		if(isActive)
		{
			text.add("I: " + (te.getInputFromSide(dir, color) ? "HIGH" : "LOW"));
			text.add("O: " + (te.getOutputToSide(dir, color) ? "HIGH" : "LOW"));
		}	
		return text;
	}
}

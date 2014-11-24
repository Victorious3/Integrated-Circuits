package vic.mod.integratedcircuits.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityPCBLayout;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.ic.CircuitProperties;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.misc.RenderUtils;

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
		if(getHoverState(field_146123_n) == 2) parent.setCurrentItem(this);
		
		GL11.glPushMatrix();
		GL11.glTranslatef(this.xPosition, this.yPosition, 0);
		GL11.glTranslatef(4F, 4F, 0F);
		GL11.glRotatef(side * 90, 0F, 0F, 1F);
		GL11.glTranslatef(-4F, -4F, -0F);
		
		ForgeDirection dir = MiscUtils.getDirection(side);
		isActive = te.getCircuitData().getProperties().getModeAtSide(side) != CircuitProperties.SIMPLE || color == 0;
		boolean isPowered = isActive && te.getInputFromSide(dir, color) || te.getOutputToSide(dir, color);
		
		if(isActive)
		{
			if(isPowered) GL11.glColor3f(0F, 1F, 0F);
			else GL11.glColor3f(0F, 0.4F, 0F);
			drawTexturedModalRect(0, 3, 5 * 8, 31 * 8, 8, 8);
		}

		GL11.glColor3f(0F, 0F, 0F);
		if(isActive) 
		{
			int c2 = 0;
			if(te.getCircuitData().getProperties().getModeAtSide(side) == CircuitProperties.ANALOG)
				c2 = (color * 17) << 20;
			else c2 = MapColor.getMapColorForBlockColored(color).colorValue;
			RenderUtils.applyColorIRGB(c2);
		}
		drawTexturedModalRect(0, 0, 4 * 8, (getHoverState(field_146123_n) == 2 || isPowered ? 30 : 31) * 8, 8, 8);
		
		if(isPowered) GL11.glColor3f(0F, 1F, 0F);
		else GL11.glColor3f(0F, 0.4F, 0F);
		drawTexturedModalRect(0, 0, 5 * 8, 30 * 8, 8, 8);
		GL11.glPopMatrix();
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
	public List<String> getHoverInformation() 
	{
		ArrayList<String> text = new ArrayList<String>();
		ForgeDirection dir = MiscUtils.getDirection(side);
		if(te.getCircuitData().getProperties().getModeAtSide(side) == CircuitProperties.ANALOG)
			text.add("S: "  + color);
		else text.add("F: 0x" + Integer.toHexString(color));
		if(isActive)
		{
			text.add("I: " + (te.getInputFromSide(dir, color) ? "HIGH" : "LOW"));
			text.add("O: " + (te.getOutputToSide(dir, color) ? "HIGH" : "LOW"));
		}
		return text;
	}
}

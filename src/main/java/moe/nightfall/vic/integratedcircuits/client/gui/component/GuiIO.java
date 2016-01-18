package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class GuiIO extends GuiButton implements IHoverable {
	public int side;
	public int color;
	private GuiCAD parent;
	private TileEntityCAD te;
	private boolean isActive;

	public GuiIO(int id, int x, int y, int color, int side, GuiCAD parent, TileEntityCAD te) {
		super(id, x, y, 9, 9, "");
		this.color = color;
		this.side = side;
		this.parent = parent;
		this.te = te;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		mc.getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width
				&& y < this.yPosition + this.height;
		this.field_146123_n = !parent.blockMouseInput && field_146123_n;
		if (getHoverState(field_146123_n) == 2)
			parent.setCurrentItem(this);

		GL11.glPushMatrix();
		GL11.glTranslatef(this.xPosition, this.yPosition, 0);
		GL11.glTranslatef(4F, 4F, 0F);
		GL11.glRotatef(side * 90, 0F, 0F, 1F);
		GL11.glTranslatef(-4F, -4F, -0F);

		ForgeDirection dir = MiscUtils.getDirection(side);
		isActive = (te.getCircuitData().getProperties().getModeAtSide(side) != EnumConnectionType.SIMPLE || color == 0) 
				&& (te.getCircuitData().getProperties().getModeAtSide(side) != EnumConnectionType.NONE);
		boolean isPowered = isActive && te.getExternalInputFromSide(dir, color) || te.getOutputToSide(dir, color);

		if (isActive) {
			if (isPowered)
				GL11.glColor3f(0F, 1F, 0F);
			else
				GL11.glColor3f(0F, 0.4F, 0F);
			drawTexturedModalRect(0, 3, 5 * 8, 31 * 8, 8, 8);
		}

		GL11.glColor3f(0F, 0F, 0F);
		if (isActive) {
			int c2 = 0;
			if (te.getCircuitData().getProperties().getModeAtSide(side) == EnumConnectionType.ANALOG)
				c2 = (color * 17) << 20;
			else
				c2 = MapColor.getMapColorForBlockColored(color).colorValue;
			RenderUtils.applyColorIRGB(c2);
		}
		drawTexturedModalRect(0, 0, 4 * 8, (getHoverState(field_146123_n) == 2 || isPowered ? 30 : 31) * 8, 8, 8);

		if (isPowered)
			GL11.glColor3f(0F, 1F, 0F);
		else
			GL11.glColor3f(0F, 0.4F, 0F);
		drawTexturedModalRect(0, 0, 5 * 8, 30 * 8, 8, 8);
		GL11.glPopMatrix();
	}

	@Override
	public boolean mousePressed(Minecraft mc, int par1, int par2) {
		boolean b = super.mousePressed(mc, par1, par2) && !parent.blockMouseInput;
		if (b && isActive) {
			ForgeDirection dir = MiscUtils.getDirection(side);
			te.setExternalInputFromSide(dir, color, !te.getExternalInputFromSide(dir, color));
		}
		return b;
	}

	@Override
	public List<String> getHoverInformation() {
		ArrayList<String> text = new ArrayList<String>();
		ForgeDirection dir = MiscUtils.getDirection(side);
		if (isActive) {
			EnumConnectionType mode = te.getCircuitData().getProperties().getModeAtSide(side);
			if (mode != EnumConnectionType.SIMPLE) {
				if (mode == EnumConnectionType.ANALOG)
					text.add("S: " + color);
				else
					text.add("F: 0x" + Integer.toHexString(color));
			}
			text.add("I: "
					+ I18n.format("gui.integratedcircuits.cad.mode."
							+ (te.getExternalInputFromSide(dir, color) ? "high" : "low")));
			text.add("O: "
					+ I18n.format("gui.integratedcircuits.cad.mode."
							+ (te.getOutputToSide(dir, color) ? "high" : "low")));
		} else
			text.add("N");
		return text;
	}
}

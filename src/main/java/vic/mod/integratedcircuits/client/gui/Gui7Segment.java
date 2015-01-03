package vic.mod.integratedcircuits.client.gui;

import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import vic.mod.integratedcircuits.client.Resources;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import vic.mod.integratedcircuits.part.Part7Segment;

public class Gui7Segment extends GuiScreen implements IHoverableHandler
{
	private Part7Segment part;
	private int xSize, ySize, guiLeft, guiTop;
	private IHoverable hoverable;
	private GuiDropdown dropdown;
	
	public Gui7Segment(Part7Segment part) 
	{
		this.part = part;
		this.xSize = 150;
		this.ySize = 100;
	}
	
	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	@Override
	public void initGui() 
	{
		guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
        
        buttonList.add(dropdown = new GuiDropdown(0, guiLeft + 60, guiTop + 23, 75, 15, Arrays.asList(
        	"Element 1", 
        	"A HYPER LONG ELEMENT TWO THAT HAS TO BE CROPPED",
        	"Element 3", 
        	"DHUIAWODG"
        )));
	}
	
	//TODO Localization
	
	@Override
	public void drawScreen(int x, int y, float par3) 
	{
		GL11.glColor3f(1, 1, 1);
		
		hoverable = null;
		drawDefaultBackground();
		
		this.mc.getTextureManager().bindTexture(Resources.RESOURCE_GUI_7SEGMENT_BACKGROUND);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(!(x >= guiLeft + 119 && x < guiLeft + 119 + 23 && y >= guiTop + 51 && y < guiTop + 51 + 33 && !dropdown.isOpen()))
			drawTexturedModalRect(guiLeft + 119, guiTop + 51, 150, 0, 23, 33);
		
		int display = part.display;
		for(int i = 0; i < 8; i++)
		{
			int off = part.display >> i & 1;
			drawTexturedModalRect(guiLeft + 86 + i * 4, guiTop + 94, 150 + off, 33, 1, 5);
		}
		
		String title = "Seven Segment Display";
		fontRendererObj.drawString(title, guiLeft + xSize / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 8, 0x333333);
		
		this.mc.getTextureManager().bindTexture(this.mc.getTextureManager().getResourceLocation(0));
		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft + 119, guiTop + 51, 0);
		GL11.glRotatef(-90, 1, 0, 0);
		Part7SegmentRenderer.render7Segment(part.display, 1, part.color);
		GL11.glPopMatrix();
		
		super.drawScreen(x, y, par3);
		
		if(hoverable != null)
			drawHoveringText(hoverable.getHoverInformation(), x , y, this.fontRendererObj);
		
		GL11.glColor3f(1, 1, 1);
	}
	
	@Override
	protected void keyTyped(char ch, int keycode) 
	{
		if(keycode == Keyboard.KEY_ESCAPE || keycode == Keyboard.KEY_E)
		{
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) 
	{
		this.hoverable = hoverable;
	}
}

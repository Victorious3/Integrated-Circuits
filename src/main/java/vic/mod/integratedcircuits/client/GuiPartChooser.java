package vic.mod.integratedcircuits.client;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.SubLogicPart;
import vic.mod.integratedcircuits.SubLogicPart.PartNull;
import vic.mod.integratedcircuits.SubLogicPartRenderer;

public class GuiPartChooser extends GuiButton
{
	public SubLogicPart current;
	private ArrayList<GuiPartChooser> list;
	public int mode;
	private boolean active = false;
	private boolean showList = false;
	private GuiPCBLayout parent;
	private GuiPartChooser chooserParent;
	
	public GuiPartChooser(int id, int x, int y, int mode, GuiPCBLayout parent)
	{
		super(id, x, y, 20, 20, "");
		this.mode = mode;
		this.parent = parent;
	}
	
	public GuiPartChooser(int id, int x, int y, SubLogicPart current, GuiPCBLayout parent)
	{
		this(id, x, y, current, null, parent);
	}
	
	public GuiPartChooser(int id, int x, int y, SubLogicPart current, ArrayList<SubLogicPart> list, GuiPCBLayout parent) 
	{
		super(id, x, y, 20, 20, "");
		this.current = current;
		if(list != null)
		{
			list.add(0, current);
			this.list = new ArrayList<GuiPartChooser>();
			for(int i = 0; i < list.size(); i++)
			{
				GuiPartChooser child = new GuiPartChooser(i, x - 21, y + i * 21, list.get(i), parent);
				child.chooserParent = this;
				child.visible = false;
				this.list.add(child);
			}
		}	
		mode = 0;
		this.parent = parent;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) 
	{
		super.drawButton(mc, x, y);
		mc.getTextureManager().bindTexture(new ResourceLocation(IntegratedCircuits.modID, "textures/gui/sublogicpart.png"));
		if(mode == 0) SubLogicPartRenderer.renderPart(current, this.xPosition + 2, this.yPosition + 2);
		else drawTexturedModalRect(this.xPosition + 2, this.yPosition + 1, (4 + mode) * 16, 15 * 16, 16, 16);
		
		if(showList && list != null)
		{
			drawRect(xPosition, yPosition - 1, xPosition + width + 1, yPosition + height + 1, 180 << 24);
			drawRect(xPosition - 22, yPosition - 1, xPosition, yPosition + list.size() * 21, 180 << 24);
			for(GuiPartChooser child : list)
			{
				child.drawButton(mc, x, y);
			}
		}
		
		if(x > xPosition && y > yPosition && x < xPosition + width && y < yPosition + height)
		{
			parent.hoveredChooser = this;
		}
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) 
	{
		boolean bool = super.mousePressed(mc, x, y);	
		if(showList && list != null)
		{
			for(GuiPartChooser child : list)
			{
				if(child.mousePressed(mc, x, y))
				{
					child.func_146113_a(mc.getSoundHandler());
					parent.selectedChooser = child;
				}
			}
		}
		if(!bool && list != null) 
		{
			showList = false;
			parent.blockMouseInput = false;
		}	
		return bool;
	}

	@Override
	public void mouseReleased(int x, int y) 
	{
		if(!active)
		{
			if(chooserParent == null)
				for(Object obj : parent.getButtonList())
					if(obj instanceof GuiPartChooser) ((GuiPartChooser)obj).setActive(false);
					
			active = true;
			
			if(chooserParent != null) 
			{
				for(GuiPartChooser child : chooserParent.list)
				{
					child.setActive(false);
				}
				setActive(true);
				chooserParent.current = this.current;
			}
			
			if(mode == 1) parent.selectedPart = null;
			else if(mode == 2) parent.selectedPart = SubLogicPartRenderer.createEncapsulated(PartNull.class);
			else parent.selectedPart = this.current;
		}
		if(list != null)
		{
			showList = !showList;
			parent.blockMouseInput = showList;
			for(GuiPartChooser child : list)
			{
				child.visible = showList;
			}
		}		
	}

	@Override
	public int getHoverState(boolean par1) 
	{
		return !enabled ? 0 : active ? 2 : par1 ? 2 : 1;
	}
}

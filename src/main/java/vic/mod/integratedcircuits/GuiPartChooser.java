package vic.mod.integratedcircuits;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class GuiPartChooser extends GuiButton
{
	private SubLogicPart current;
	private ArrayList<GuiPartChooser> list;
	private int mode;
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
		if(mode == 0) SubLogicPartRenderer.renderPart(current, this, this.xPosition + 2, this.yPosition + 2);
		else SubLogicPartRenderer.drawTexture((4 + mode) * 16, 15 * 16, this, this.xPosition + 2, this.yPosition + 1);
		
		if(showList && list != null)
		{
			for(GuiPartChooser child : list)
			{
				child.drawButton(mc, x, y);
			}
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
				child.setActive(false);
				if(child.mousePressed(mc, x, y))
				{
					child.func_146113_a(mc.getSoundHandler());
					parent.selectedChooser = child;
				}
			}
		}
		if(!bool && list != null) showList = false;
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
			if(chooserParent != null) chooserParent.current = this.current;
			if(mode == 1) parent.selectedPart = null;
			else if(mode == 2) parent.selectedPart = SubLogicPartRenderer.createEncapsulated(null);
			else parent.selectedPart = this.current;
		}
		if(list != null)
		{
			showList = !showList;
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

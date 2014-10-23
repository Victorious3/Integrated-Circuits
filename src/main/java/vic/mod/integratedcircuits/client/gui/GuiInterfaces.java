package vic.mod.integratedcircuits.client.gui;

import java.util.List;

import vic.mod.integratedcircuits.client.gui.GuiCallback.Action;

public abstract class GuiInterfaces 
{
	public interface IHoverable 
	{
		public List<String> getHoverInformation();
	}
	
	public interface IHoverableHandler
	{
		public void setCurrentItem(IHoverable hoverable);
	}
	
	public interface IGuiCallback
	{
		public void onCallback(GuiCallback gui, Action result, int id);
	}
}
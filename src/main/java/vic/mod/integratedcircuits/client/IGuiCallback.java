package vic.mod.integratedcircuits.client;

import vic.mod.integratedcircuits.client.GuiCallback.Action;

public interface IGuiCallback
{
	public void onCallback(GuiCallback gui, Action result, int id);
}
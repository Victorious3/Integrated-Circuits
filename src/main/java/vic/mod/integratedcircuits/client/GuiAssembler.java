package vic.mod.integratedcircuits.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import vic.mod.integratedcircuits.ContainerAssembler;

public class GuiAssembler extends GuiContainer
{
	public GuiAssembler(ContainerAssembler container) 
	{
		super(container);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		drawDefaultBackground();
	}
}

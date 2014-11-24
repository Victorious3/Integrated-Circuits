package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerAssembler;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiAssembler extends GuiContainer
{
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/assembler.png");
	private TileEntityAssembler te;
	
	public GuiAssembler(ContainerAssembler container) 
	{
		super(container);
		this.xSize = 176;
		this.ySize = 222;
		this.te = container.tileentity;
	}
	
	@Override
	public void initGui() 
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonExt(0, guiLeft + 40, guiTop + 82, 100, 20, EnumChatFormatting.LIGHT_PURPLE + "Magic Button!"));
	}

	@Override
	protected void actionPerformed(GuiButton button) 
	{
		if(button.id == 0)
		{
			IntegratedCircuits.networkWrapper.sendToServer(new PacketAssemblerStart(te.xCoord, te.yCoord, te.zCoord));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		GL11.glColor3f(1, 1, 1);
		drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
		GL11.glColor3f(1, 1, 1);
	}
}

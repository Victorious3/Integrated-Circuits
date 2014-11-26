package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerAssembler;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiAssembler extends GuiContainer implements IHoverableHandler
{
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/assembler.png");
	public TileEntityAssembler te;
	private GuiCraftingList craftingList;
	private IHoverable hoverable;
	public ContainerAssembler container;
	
	public GuiAssembler(ContainerAssembler container) 
	{
		super(container);
		this.xSize = 176;
		this.ySize = 222;
		this.te = container.tileentity;
		this.container = container;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		craftingList = new GuiCraftingList(this, mc, guiLeft + 29, guiTop + 26, 110, 62);
		this.buttonList.add(new GuiButtonExt(0, guiLeft + 40, guiTop + 90, 100, 20, EnumChatFormatting.LIGHT_PURPLE + "Magic Button!"));
		refreshUI();
	}
	
	public void refreshUI()
	{
		if(te.cdata != null)
			craftingList.setCraftingAmount(te.cdata.getCost());
	}

	@Override
	protected void actionPerformed(GuiButton button) 
	{
		if(button.id == 0)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketAssemblerStart(te.xCoord, te.yCoord, te.zCoord));
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		if(button != 0 || !craftingList.mouseClicked(x, y, button))
			super.mouseClicked(x, y, button);
	}
	
	@Override
	protected void mouseMovedOrUp(int x, int y, int button)
	{
		if(button != 0 || !craftingList.mouseMovedOrUp(x, y, button))
			super.mouseMovedOrUp(x, y, button);
	}
	
	@Override
	public void drawScreen(int x, int y, float par3) 
	{
		super.drawScreen(x, y, par3);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		GL11.glColor3f(1, 1, 1);
		drawDefaultBackground();
		hoverable = null;
		
		this.mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
		
		fontRendererObj.drawString("Assembler", guiLeft + 30, guiTop + 12, 0x333333);
		
		craftingList.drawScreen(x, y, par1);
		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{
		if(hoverable != null)
			drawHoveringText(hoverable.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) 
	{
		this.hoverable = hoverable;
	}
}

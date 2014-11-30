package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerAssembler;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import vic.mod.integratedcircuits.ic.CircuitProperties;
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
		
		this.buttonList.add(new GuiButtonExt(0, guiLeft + 78, guiTop + 95, 10, 10, "+"));
		this.buttonList.add(new GuiButtonExt(1, guiLeft + 40, guiTop + 95, 10, 10, "-"));
		this.buttonList.add(new GuiButtonExt(2, guiLeft + 96, guiTop + 93, 30, 14, "Run"));
		
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
		if(button.id < 2)
		{
			if(button.id == 0)
				te.request++;
			else if(button.id == 1)
				te.request--;
			te.request = (byte)MathHelper.clamp_int(te.request, 1, 64);
		}
		else if(button.id == 2)
			IntegratedCircuits.networkWrapper.sendToServer(new PacketAssemblerStart(te.xCoord, te.yCoord, te.zCoord, te.request));
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
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y) 
	{
		GL11.glColor3f(1, 1, 1);
		drawDefaultBackground();
		hoverable = null;
		
		this.mc.getTextureManager().bindTexture(backgroundTexture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

		fontRendererObj.drawString(String.valueOf(te.request), guiLeft + 60, guiTop + 96, 0);
		
		if(te.cdata != null)
		{
			CircuitProperties prop = te.cdata.getProperties();
			fontRendererObj.drawString(prop.getName() + " (" + te.size + "x" + te.size + ")", guiLeft + 30, guiTop + 12, 0x333333);
		}
		else fontRendererObj.drawString(EnumChatFormatting.ITALIC + "-No Circuit-", guiLeft + 30, guiTop + 12, 0x333333);
		craftingList.drawScreen(x, y, par1);
		GL11.glColor3f(1, 1, 1);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{
		if(hoverable != null)
			drawHoveringText(hoverable.getHoverInformation(), x - guiLeft, y - guiTop, this.fontRendererObj);
		RenderHelper.enableGUIStandardItemLighting();
	}

	@Override
	public void setCurrentItem(IHoverable hoverable) 
	{
		this.hoverable = hoverable;
	}
}

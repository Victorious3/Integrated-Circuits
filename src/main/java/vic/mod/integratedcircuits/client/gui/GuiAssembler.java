package vic.mod.integratedcircuits.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.ContainerAssembler;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import vic.mod.integratedcircuits.client.gui.GuiInterfaces.IHoverableHandler;
import vic.mod.integratedcircuits.ic.CircuitProperties;
import vic.mod.integratedcircuits.misc.Vec2;
import vic.mod.integratedcircuits.net.PacketAssemblerStart;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiAssembler extends GuiContainer implements IHoverableHandler
{
	private static final ResourceLocation backgroundTexture = new ResourceLocation(IntegratedCircuits.modID, "textures/gui/assembler.png");
	public TileEntityAssembler te;
	private GuiCraftingList craftingList;
	private GuiStateLabel labelAutomaticPull;
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
		
		this.buttonList.add(new GuiButtonExt(0, guiLeft + 71, guiTop + 95, 10, 10, "+"));
		this.buttonList.add(new GuiButtonExt(1, guiLeft + 33, guiTop + 95, 10, 10, "-"));
		this.buttonList.add(new GuiButtonExt(2, guiLeft + 89, guiTop + 93, 30, 14, "Run"));
		this.buttonList.add(new GuiButtonExt(3, guiLeft + 122, guiTop + 93, 14, 14, "x"));

		labelAutomaticPull = new GuiStateLabel(this, 4, guiLeft + 9, guiTop + 94, 14, 14, backgroundTexture)
			.addState(new Vec2(176, 0), new Vec2(176, 14))
			.addDescription("Single Pull", "Automatic Pull")
			.setState(te.getOptionSet().getInt(te.SETTING_PULL));
		
		this.buttonList.add(labelAutomaticPull);
		
		refreshUI();
	}
	
	public void refreshUI()
	{
		if(te.cdata != null)
			craftingList.setCraftingAmount(te.cdata.getCost());
		labelAutomaticPull.setState(te.getOptionSet().getInt(te.SETTING_PULL));
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
		else if(button.id == 4)
			te.getOptionSet().changeSetting(te.SETTING_PULL, ((GuiStateLabel)button).getState());
		else IntegratedCircuits.networkWrapper.sendToServer(
			new PacketAssemblerStart(te.xCoord, te.yCoord, te.zCoord, (byte)(te.request * (button.id == 2 ? 1 : 0))));
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

		drawRect(guiLeft + 46, guiTop + 94, guiLeft + 68, guiTop + 106, 0xFFA0A0A0);
		drawRect(guiLeft + 47, guiTop + 95, guiLeft + 67, guiTop + 105, 0xFF000000);
		drawCenteredString(fontRendererObj, String.valueOf(te.request), guiLeft + 57, guiTop + 96, 0xE0E0E0);
		
		if(te.cdata != null)
		{
			CircuitProperties prop = te.cdata.getProperties();
			fontRendererObj.drawString(prop.getName() + " (" + te.size + "x" + te.size + ")", guiLeft + 30, guiTop + 12, 0x333333);
		}
		else fontRendererObj.drawString(EnumChatFormatting.ITALIC + "-No Circuit-", guiLeft + 30, guiTop + 12, 0x333333);
		
		craftingList.drawScreen(x, y, par1);
		
		//When the assembler is running, progress bar
		if(te.getStatus() != te.IDLE)
		{
			float progress = te.laserHelper.getPosition() / (float)te.cdata.getPartAmount();
			float total = te.getQueuePosition() / (float)te.getQueueSize();
			int i2 = MathHelper.clamp_int((int)(progress * 100), 0, 100);
			int i3 = MathHelper.clamp_int((int)(total * 100), 0, 100);
			
			drawRect(guiLeft + 29, guiTop + 26, guiLeft + 139, guiTop + 88, 0xBB000000);
			int i1 = ClientProxy.clientTicks % 30 / 10 + 1;
			
			if(te.getStatus() != te.RUNNING) 
			{
				i1 = 3;
				i2 = 0;
			}
			
			fontRendererObj.drawString("Processing" + StringUtils.repeat('.', i1), guiLeft + 52, guiTop + 32, 0xFFFFFF);
			drawRect(guiLeft + 33, guiTop + 49, guiLeft + 135, guiTop + 71, 0xFF000000);
			
			drawRect(guiLeft + 34, guiTop + 50, guiLeft + 134, guiTop + 65, 0xFF515151);
			drawRect(guiLeft + 34, guiTop + 50, guiLeft + 34 + i2, guiTop + 65, 0xFF535C92);
			drawCenteredString(fontRendererObj, i2 + "%", guiLeft + 84, guiTop + 54, 0xFFFFFF);
			
			drawRect(guiLeft + 34, guiTop + 66, guiLeft + 134, guiTop + 70, 0xFF515151);
			drawRect(guiLeft + 34, guiTop + 66, guiLeft + 34 + i3, guiTop + 70, 0xFF535C92);

			if(te.getStatus() != te.RUNNING)
			{
				int color = (int)((Math.sin((ClientProxy.clientTicks + par1) * 0.5) * 0.2 + 0.2) * 255 + 153);
				drawCenteredString(fontRendererObj, I18n.format("gui.integratedcircuits.assembler.statuscode." + te.getStatus()), guiLeft + 84, guiTop + 76, color << 16);
			}
		}

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

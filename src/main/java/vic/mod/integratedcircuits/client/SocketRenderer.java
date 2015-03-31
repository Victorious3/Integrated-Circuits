package vic.mod.integratedcircuits.client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.client.model.ModelBase;
import vic.mod.integratedcircuits.gate.GateRegistry;
import vic.mod.integratedcircuits.gate.IGate;
import vic.mod.integratedcircuits.gate.ISocket;
import vic.mod.integratedcircuits.item.IGateItem;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Transformation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SocketRenderer extends PartRenderer<ISocket>
{	
	private IGate part;
	private BlockCoord pos;
	private float partialTicks;
	
	public SocketRenderer(String iconName)
	{
		models.add(new ModelBase(iconName));
	}

	public void prepare(ISocket part) 
	{
		pos = part.getPos();
		this.part = part.getGate();
		if(this.part != null) GateRegistry.getRenderer(this.part.getClass()).prepare(this.part);
	}
	
	@Override
	public void prepareInv(ItemStack stack)
	{
		pos = null;
		this.part = null;
	}

	public void prepareDynamic(ISocket part, float partialTicks) 
	{
		this.partialTicks = partialTicks;
		pos = part.getPos();
		this.part = part.getGate();
		if(this.part != null) GateRegistry.getRenderer(this.part.getClass()).prepareDynamic(this.part, partialTicks);
	}
	
	public void renderStatic(Transformation t, int orient)
	{
		super.renderStatic(t, orient);
		if(this.part != null) GateRegistry.getRenderer(this.part.getClass()).renderStatic(t, orient);
	}
	
	public void renderDynamic(Transformation t) 
	{
		if(this.part != null) GateRegistry.getRenderer(this.part.getClass()).renderDynamic(t);
		else if(pos != null)
		{
			ItemStack stack;
			if((stack = Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem()) != null && stack.getItem() instanceof IGateItem)
			{
    			MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
    			if(mop.typeOfHit == MovingObjectType.BLOCK && mop.blockX == pos.x && mop.blockY == pos.y && mop.blockZ == pos.z)
    			{	
    				String gateID = ((IGateItem)stack.getItem()).getGateID(stack, Minecraft.getMinecraft().thePlayer, pos);
    				IPartRenderer<IGate> renderer = GateRegistry.getRenderer(gateID);
    				
    				TextureUtils.bindAtlas(0);
    				CCRenderState.reset();
    				CCRenderState.setDynamic();
    				CCRenderState.pullLightmap();
    				
    				GL11.glEnable(GL11.GL_BLEND);
    				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    				GL11.glEnable(GL11.GL_ALPHA_TEST);
    				
    				renderer.prepareInv(stack);
    				CCRenderState.alphaOverride = (int)(120 + 20 * Math.sin((ClientProxy.clientTicks + partialTicks) / 5D));
    				CCRenderState.startDrawing();
    				renderer.renderStatic(t, 0);
    				CCRenderState.draw();
    				renderer.renderDynamic(t);
    				CCRenderState.alphaOverride = 0;
    				
    				GL11.glDisable(GL11.GL_ALPHA_TEST);
    				GL11.glDisable(GL11.GL_BLEND);
    			}
			}
		}
	}
}

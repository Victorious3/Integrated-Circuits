package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.api.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.model.ModelChip;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.ic.CircuitProperties;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.opengl.GL11;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Transformation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PartCircuitRenderer extends PartRenderer<GateCircuit>
{
	private byte tier;
	private String name = "NO_NAME";

	public PartCircuitRenderer()
	{
		models.add(new ModelChip());
	}
	
	@Override
	public void prepare(GateCircuit part) 
	{
		CircuitProperties prop = part.getCircuitData().getProperties();
		int bundled = 0;
		for(int i = 0; i < 4; i++)
			bundled |= prop.getModeAtSide((i + 2) % 4) == EnumConnectionType.BUNDLED ? 1 << i : 0;
//		prepareBundled(bundled);
//		prepareRedstone(~bundled, part.io);
	}
	
	@Override
	public void prepareInv(ItemStack stack)
	{
		NBTTagCompound comp = stack.getTagCompound();	
		if(comp == null) return;
		NBTTagCompound comp2 = comp.getCompoundTag("circuit").getCompoundTag("properties");
		byte con = comp2.getByte("con");
		
		int bundled = 0;
		for(int i = 0; i < 4; i++)
			bundled |= (con >> ((i + 2) % 4) * 2 & 3) == EnumConnectionType.BUNDLED.ordinal() ? 1 << i : 0;
//		prepareBundled(bundled);
//		prepareRedstone(~bundled, 0);
		
		name = comp2.getString("name");
		tier = (byte) (Math.log(comp.getCompoundTag("circuit").getInteger("size")) / Math.log(2) - 3);
	}
	
	@Override
	public void prepareDynamic(GateCircuit part, float partialTicks) 
	{
		tier = (byte) (Math.log(part.circuitData.getSize()) / Math.log(2) - 3);
		name = part.circuitData.getProperties().getName();
	}

	@Override
	public void renderDynamic(Transformation t)
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		t.glApply();
		GL11.glRotatef(90, 1, 0, 0);
		GL11.glRotatef(180, 0, 0, 1);
		GL11.glTranslated(-13 / 16D, -5 / 16D, -5.005 / 16D);
		
		FontRenderer fr = RenderManager.instance.getFontRenderer();
		if(fr == null) return;
		
		GL11.glPushMatrix();
		GL11.glScaled(1 / 64D, 1 / 64D, 1 / 64D);
		fr.drawString("T" + tier, 0, 0, 0xFFFFFF | CCRenderState.alphaOverride << 24);
		GL11.glPopMatrix();
		
		GL11.glTranslated(0, -4 / 16D, 0);
		GL11.glScaled(1 / 64D, 1 / 64D, 1 / 64D);
		
		int w = fr.getStringWidth(name);
		int mw = 42;
		fr.drawString(name, (int)(mw / 2F - w / 2F), 0, 0xFFFFFF | CCRenderState.alphaOverride << 24);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}
}

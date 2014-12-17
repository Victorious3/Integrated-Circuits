package vic.mod.integratedcircuits.client;

import java.util.Arrays;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitProperties;
import vic.mod.integratedcircuits.part.PartCircuit;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;

/** https://github.com/MrTJP/ProjectRed/ **/
public class PartCircuitRenderer extends PartRenderer<PartCircuit>
{
	private static PinModel[] pinModels = {new PinModel(0), new PinModel(1), new PinModel(2), new PinModel(3)};
	public static IIcon iconIC;
	public static IIcon iconGold;
	
	public PartCircuitRenderer()
	{
		models.add(new ChipModel());
		models.addAll(Arrays.asList(pinModels));
	}

	public static class ChipModel implements IComponentModel
	{
		private static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
		{
			for(int i = 0; i < 24; i++) models[i] = bakeCopy(base, i);
		}

		@Override
		public void renderModel(Transformation arg0, int arg1)
		{
			models[arg1 % 24].render(arg0, new IconTransformation(iconIC));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 0, 0, 0, 12 / 16D, 3 / 16D, 12 / 16D);
			m1.apply(new Translation(2 / 16D, 2 / 16D, 2 / 16D));
			m1.computeNormals();
			return m1;
		}
	}
	
	public static class PinModel implements IComponentModel
	{
		private static CCModel[] bundeledModels = new CCModel[24];
		private static CCModel[] normalModels = new CCModel[24];
		public boolean isBundeled = true;
		private final int rotation;
		
		static
		{
			for(int i = 0; i < 24; i++) normalModels[i] = bakeCopy(generateModel(false), i);
			for(int i = 0; i < 24; i++) bundeledModels[i] = bakeCopy(generateModel(true), i);
		}
		
		public PinModel(int rotation)
		{
			this.rotation = rotation;
		}

		@Override
		public void renderModel(Transformation arg0, int arg1)
		{
			arg1 = arg1 & 28 | ((arg1 + rotation) & 3);
			if(isBundeled) bundeledModels[arg1 % 24].render(arg0, new IconTransformation(iconGold));
			else normalModels[arg1 % 24].render(arg0, new IconTransformation(iconGold));
		}
		
		private static CCModel generateModel(boolean isBundeled)
		{
			CCModel m1 = CCModel.quadModel(24);
			double d1 = isBundeled ? 6 : 2;
			double d2 = isBundeled ? 5 : 7;
			m1.generateBlock(0, 0, 0, 0, d1 / 16D, 4 / 16D, 2 / 16D);
			m1.apply(new Translation(d2 / 16D, 0, 0));
			m1.computeNormals();
			return m1;
		}
	}
	
	private byte tier;
	private String name = "NO_NAME";

	@Override
	public void prepare(PartCircuit part) 
	{
		CircuitProperties prop = part.getCircuitData().getProperties();
		pinModels[2].isBundeled = prop.getModeAtSide(0) == CircuitProperties.BUNDLED;
		pinModels[3].isBundeled = prop.getModeAtSide(1) == CircuitProperties.BUNDLED;
		pinModels[0].isBundeled = prop.getModeAtSide(2) == CircuitProperties.BUNDLED;
		pinModels[1].isBundeled = prop.getModeAtSide(3) == CircuitProperties.BUNDLED;
	}
	
	@Override
	public void prepareInv(ItemStack stack)
	{
		NBTTagCompound comp = stack.getTagCompound();	
		if(comp == null) return;
		NBTTagCompound comp2 = comp.getCompoundTag("circuit").getCompoundTag("properties");
		byte con = comp2.getByte("con");
		pinModels[2].isBundeled = (con & 3) == CircuitProperties.BUNDLED;
		pinModels[3].isBundeled = (con & 12) >> 2 == CircuitProperties.BUNDLED;
		pinModels[0].isBundeled = (con & 48) >> 4 == CircuitProperties.BUNDLED;
		pinModels[1].isBundeled = (con & 192) >> 6 == CircuitProperties.BUNDLED;
		name = comp2.getString("name");
		tier = (byte) (comp.getCompoundTag("circuit").getInteger("size") / 16);
	}
	
	@Override
	public void prepareDynamic(PartCircuit part, float partialTicks) 
	{
		tier = (byte)(part.circuitData.getSize() / 16);
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
		fr.drawString("T" + tier, 0, 0, 0xFFFFFF);
		GL11.glPopMatrix();
		
		GL11.glTranslated(0, -4 / 16D, 0);
		GL11.glScaled(1 / 64D, 1 / 64D, 1 / 64D);
		
		int w = fr.getStringWidth(name);
		int mw = 42;
		fr.drawString(name, (int)(mw / 2F - w / 2F), 0, 0xFFFFFF);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public void registerIcons(IIconRegister arg0) 
	{
		super.registerIcons(arg0);
		iconIC = arg0.registerIcon(IntegratedCircuits.modID + ":ic");
		iconGold = arg0.registerIcon("gold_block");
	}
}

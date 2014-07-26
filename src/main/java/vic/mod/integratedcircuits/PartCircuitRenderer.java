package vic.mod.integratedcircuits;

import mrtjp.projectred.integration.ComponentStore;
import mrtjp.projectred.integration.ComponentStore.ComponentModel;
import mrtjp.projectred.integration.RenderGate.GateRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import scala.actors.threadpool.Arrays;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;

public class PartCircuitRenderer extends GateRenderer<PartCircuit>
{
	PinModel[] pinModels = new PinModel[]{new PinModel(0), new PinModel(1), new PinModel(2), new PinModel(3)};
			
	public PartCircuitRenderer()
	{
		models.add(new ChipModel());
		models.addAll(Arrays.asList(pinModels));	
	}
	
	@Override
	public void prepare(PartCircuit part) 
	{
		pinModels[0].isBundeled = (part.state & 1) > 0;
		pinModels[1].isBundeled = (part.state & 2) > 0;
		pinModels[2].isBundeled = (part.state & 4) > 0;
		pinModels[3].isBundeled = (part.state & 8) > 0;
		super.prepare(part);
	}
	
	public static class ChipModel extends ComponentModel
	{
		private static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
        {
        	for(int i = 0; i < 24; i++) models[i] = ComponentStore.bakeCopy(base, i);
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
	
	public static class PinModel extends ComponentModel
	{
		private static CCModel[] bundeledModels = new CCModel[24];
		private static CCModel[] normalModels = new CCModel[24];
		private static CCModel normal = generateModel(false);
		private static CCModel bundeled = generateModel(true);
		
		static
        {
        	for(int i = 0; i < 24; i++) normalModels[i] = ComponentStore.bakeCopy(normal, i);
        	for(int i = 0; i < 24; i++) bundeledModels[i] = ComponentStore.bakeCopy(bundeled, i);
        }
		
		public PinModel(int rotation)
		{
			this.rotation = rotation;
		}
		
		public boolean isBundeled = true;
		private int rotation;

		@Override
		public void renderModel(Transformation arg0, int arg1)
		{
			int i1 = arg1 / 4 * 4;
			int i2 = arg1 + rotation;
			int i3 = Math.min(i1 + 3, i2);
			int i4 = (i2 - i3) > 0 ? i1 + (i2 - i3) - 1 : i3;
			if(isBundeled) bundeledModels[i4 % 24].render(arg0, new IconTransformation(iconGold));
			else normalModels[i4 % 24].render(arg0, new IconTransformation(iconGold));
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
	
	private short tier;
	private String name;

	@Override
	public void prepareDynamic(PartCircuit part, float frame) 
	{
		super.prepareDynamic(part, frame);
		tier = part.tier;
		name = part.name;
	}

	@Override
	public void renderDynamic(Transformation t)
	{
		super.renderDynamic(t);
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		t.glApply();	
		new Rotation(Math.toRadians(90), 1, 0, 0).glApply();
		new Rotation(Math.toRadians(180), 0, 0, 1).glApply();
		new Translation(-13 / 16D, -5 / 16D, -5.005 / 16D).glApply();
		
		FontRenderer fr = RenderManager.instance.getFontRenderer();
		
		GL11.glPushMatrix();
		new Scale(1 / 64D).glApply();;
		fr.drawString("T" + tier, 0, 0, 0xFFFFFF);
		GL11.glPopMatrix();
		
		new Translation(0, -4 / 16D, 0).glApply();
		
		GL11.glPushMatrix();
		new Scale(1 / 64D).glApply();
		int w = fr.getStringWidth(name);
		int mw = 42;
		fr.drawString(name, (int)(mw / 2F - w / 2F), 0, 0xFFFFFF);
		GL11.glPopMatrix();
		
		GL11.glPopMatrix();
	}

	public static IIcon iconIC;
	public static IIcon iconGold;
	
	@Override
	public void registerIcons(IIconRegister arg0) 
	{
		super.registerIcons(arg0);
		iconIC = arg0.registerIcon(IntegratedCircuits.modID + ":ic");
		iconGold = arg0.registerIcon("gold_block");
	}
}

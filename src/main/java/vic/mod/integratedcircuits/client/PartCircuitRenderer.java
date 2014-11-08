package vic.mod.integratedcircuits.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import mrtjp.projectred.integration.ComponentStore;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.part.PartCircuit;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.Vertex5;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

/** https://github.com/MrTJP/ProjectRed/ **/
public class PartCircuitRenderer
{
	private List<ComponentModel> models = new LinkedList<ComponentModel>();
	PinModel[] pinModels = new PinModel[]{new PinModel(0), new PinModel(1), new PinModel(2), new PinModel(3)};
	
	public PartCircuitRenderer()
	{
		models.add(new BaseModel());
		models.add(new ChipModel());
		models.addAll(Arrays.asList(pinModels));	
	}
	
	public void prepare(PartCircuit part) 
	{
		pinModels[2].isBundeled = (part.state & 1) != 0;
		pinModels[3].isBundeled = (part.state & 2) != 0;
		pinModels[0].isBundeled = (part.state & 4) != 0;
		pinModels[1].isBundeled = (part.state & 8) != 0;
	}
	
	public void prepareInv(ItemStack stack)
	{
		NBTTagCompound comp = stack.getTagCompound();	
		if(comp == null) return;
		byte con = comp.getByte("con");
		pinModels[2].isBundeled = (con & 1) != 0;
		pinModels[3].isBundeled = (con & 2) != 0;
		pinModels[0].isBundeled = (con & 4) != 0;
		pinModels[1].isBundeled = (con & 8) != 0;
		name = comp.getString("name");
		tier = comp.getByte("tier");
	}
	
	/** https://github.com/MrTJP/ProjectRed/blob/master/src/mrtjp/projectred/integration/ComponentStore.java **/
	public static CCModel bakeCopy(CCModel base, int orient)
	{
		CCModel m = base.copy();
		if(orient >= 24)
		{
			for(int i = 0; i < m.verts.length; i += 4)
			{
				Vertex5 vtmp = m.verts[i + 1];
				Vector3 ntmp = m.normals()[i + 1];
				m.verts[i + 1] = m.verts[i + 3];
				m.normals()[i+1] = m.normals()[i+3];
				m.verts[i + 3] = vtmp;
				m.normals()[i + 3] = ntmp;
			}
		}
		
		Transformation t = Rotation.sideOrientation(orient % 24 >> 2, orient & 3);
		if(orient >= 24) t = new Scale(-1, 1, 1).with(t);
		
		m.apply(t.at(Vector3.center)).computeLighting(LightModel.standardLightModel);
		return m;
	}
	
	public static abstract class ComponentModel
	{
		public abstract void renderModel(Transformation t, int orient);
	}
	
	public static class BaseModel extends ComponentModel
	{
		public static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
		{
			for(int i = 0; i < 24; i++) models[i] = bakeCopy(base, i);
		}

		@Override
		public void renderModel(Transformation t, int orient)
		{
			models[orient%24].render(t, new IconTransformation(iconBase));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 0.0002, 0.0002, 0.0002, 0.9998, 2 / 16D - 0.0002, 0.9998);
			m1.computeNormals();
			return m1;
		}
	}

	public static class ChipModel extends ComponentModel
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
	
	private byte tier;
	private String name = "NO_NAME";

	public void prepareDynamic(PartCircuit part, float frame) 
	{
		tier = part.tier;
		name = part.name;
	}
	
	public void renderStatic(Transformation t, int orient)
	{
		for(ComponentModel m : models) m.renderModel(t, orient);
	}

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

	public static IIcon iconIC;
	public static IIcon iconGold;
	public static IIcon iconBase;
	
	public void registerIcons(IIconRegister arg0) 
	{
		iconIC = arg0.registerIcon(IntegratedCircuits.modID + ":ic");
		iconBase = arg0.registerIcon(IntegratedCircuits.modID + ":ic_base");
		iconGold = arg0.registerIcon("gold_block");
	}
}

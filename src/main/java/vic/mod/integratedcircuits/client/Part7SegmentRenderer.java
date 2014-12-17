package vic.mod.integratedcircuits.client;

import java.util.Arrays;

import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.misc.RenderUtils;
import vic.mod.integratedcircuits.part.Part7Segment;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class Part7SegmentRenderer extends PartRenderer<Part7Segment>
{
	private static ModelBundledConnection[] conModels = new ModelBundledConnection[] {
		new ModelBundledConnection(0), new ModelBundledConnection(1), new ModelBundledConnection(2), new ModelBundledConnection(3)};
	
	public static IIcon iconSocket;
	public static IIcon iconWire;
	public static IIcon iconWireFlipped;
	public static IIcon iconSegment;
	
	public Part7SegmentRenderer()
	{
		models.add(new ModelSocket());
		models.addAll(Arrays.asList(conModels));
	}
	
	public static class ModelSocket implements IComponentModel
	{
		public static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
		{
			for(int i = 0; i < 24; i++)
				models[i] = bakeCopy(base, i);
		}

		@Override
		public void renderModel(Transformation t, int orient)
		{
			models[orient % 24].render(t, new IconTransformation(iconSocket));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(120);
			m1.generateBlock(0, 3 / 16F, 2 / 16F, 2 / 16F, 13 / 16F, 3 / 16D, 14 / 16F);
			m1.generateBlock(24, 2 / 16F, 2 / 16F, 1 / 16F, 3 / 16F, 5 / 16D, 15 / 16F);
			m1.generateBlock(48, 13 / 16F, 2 / 16F, 1 / 16F, 14 / 16F, 5 / 16D, 15 / 16F);
			m1.generateBlock(72, 2 / 16F, 2 / 16F, 1 / 16F, 14 / 16F, 5 / 16D, 2 / 16F);
			m1.generateBlock(96, 2 / 16F, 2 / 16F, 14 / 16F, 14 / 16F, 5 / 16D, 15 / 16F);
			m1.computeNormals();
			return m1;
		}
	}
	
	public static class ModelBundledConnection implements IComponentModel
	{
		private CCModel[] models = new CCModel[24];
		private final int rotation;
		private boolean rendered = true;
		
		private ModelBundledConnection(int rotation)
		{
			this.rotation = rotation;
			CCModel model =  generateModel(rotation % 2 == 0);
			for(int i = 0; i < 24; i++)
				models[i] = bakeCopy(model, i).shrinkUVs(0.002);
		}

		@Override
		public void renderModel(Transformation t, int arg1)
		{
			if(!rendered) return;
			arg1 = arg1 & 28 | ((arg1 + rotation) & 3);
			ForgeDirection dir = ForgeDirection.getOrientation((arg1 & 3) + 2);
			ForgeDirection dir1 = ForgeDirection.getOrientation((arg1 & 28) >> 2).getRotation(ForgeDirection.UP);
			boolean b = ((dir1.ordinal() % 2 == 0 ? 12 : 9) & dir.flag >> 2) > 0;
			models[arg1 % 24].render(t, new IconTransformation(b ? iconWireFlipped : iconWire));
		}
		
		private static CCModel generateModel(boolean small)
		{
			CCModel m1 = CCModel.quadModel(24);
			double d = small ? 1 : 2;
			m1.generateBlock(0, 5 / 16D, 0, 0, 11 / 16D, 4 / 16D, d / 16D);
			m1.computeNormals();
			return m1;
		}
	}
	
	private int display;
	
	@Override
	public void prepare(Part7Segment part) 
	{
		for(int i = 0; i < 4; i++)
			conModels[i].rendered = true;
	}

	@Override
	public void prepareInv(ItemStack stack) 
	{
		for(int i = 0; i < 4; i++)
			conModels[i].rendered = false;
	}
	
	@Override
	public void prepareDynamic(Part7Segment part, float partialTicks) 
	{
		display = part.display;
	}

	@Override
	public void renderDynamic(Transformation t) 
	{
		GL11.glPushMatrix();
		t.glApply();
		GL11.glTranslatef(0.5F, 0, 0.5F);
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslatef(-0.5F, 0, -0.5F);
		RenderUtils.setBrightness(240, 240);
		GL11.glColor3f(0, 1, 0);
		Tessellator tes = Tessellator.instance;
		double u1 = iconSegment.getInterpolatedU(0);
		double u2 = iconSegment.getInterpolatedU(11.5);
		double v1 = iconSegment.getInterpolatedV(0.5);
		double v2 = iconSegment.getInterpolatedV(16);
		double y = 3 / 16F + 0.002F;
		tes.startDrawingQuads();
		tes.setNormal(0, 1, 0);
		tes.addVertexWithUV(3.5 / 16F, y, 2.5 / 16F, u1, v1);
		tes.addVertexWithUV(3.5 / 16F, y, 13.5 / 16F, u1, v2);
		tes.addVertexWithUV(12.5 / 16F, y, 13.5 / 16F, u2, v2);
		tes.addVertexWithUV(12.5 / 16F, y, 2.5 / 16F, u2, v1);
		tes.draw();
		RenderUtils.resetBrightness();
		GL11.glPopMatrix();
	}

	@Override
	public void registerIcons(IIconRegister arg0) 
	{
		super.registerIcons(arg0);
		iconSocket = arg0.registerIcon(IntegratedCircuits.modID + ":ic_uniform");
		iconWire = arg0.registerIcon(IntegratedCircuits.modID + ":ic_wire");
		iconWireFlipped = new IconFlipped(iconWire, true, false);
		iconSegment = arg0.registerIcon(IntegratedCircuits.modID + ":ic_segment");
	}
}

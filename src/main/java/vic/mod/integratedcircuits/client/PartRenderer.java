package vic.mod.integratedcircuits.client;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.Vertex5;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;

public class PartRenderer <T extends TMultiPart> implements IItemRenderer
{	
	protected List<IComponentModel> models = new LinkedList<IComponentModel>();
	
	public static IIcon iconBase;
	
	public PartRenderer()
	{
		models.add(new BaseModel());
	}
	
	public static class BaseModel implements IComponentModel
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
			models[orient % 24].render(t, new IconTransformation(iconBase));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 0, 0, 0, 1, 2 / 16D, 1);
			m1.computeNormals();
			shrink(m1, 0.0002);
			return m1;
		}
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
	
	public static CCModel shrink(CCModel model, double inset)
	{
		for(int i = 0; i < model.verts.length; i++)
			model.verts[i].vec.subtract(model.normals()[i].copy().multiply(inset));
		return model;
	}
	
	public static interface IComponentModel
	{
		public void renderModel(Transformation t, int orient);
	}
	
	public void prepare(T part) 
	{
		
	}
	
	public void prepareInv(ItemStack stack)
	{
		
	}
	
	public void prepareDynamic(T part, float partialTicks) 
	{
		
	}
	
	public void renderStatic(Transformation t, int orient)
	{
		for(IComponentModel m : models) m.renderModel(t, orient);
	}
	
	public void renderDynamic(Transformation t)
	{
		
	}
	
	public void registerIcons(IIconRegister arg0) 
	{
		iconBase = arg0.registerIcon(IntegratedCircuits.modID + ":ic_base");
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) 
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) 
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
	{
		switch (type) {
		case ENTITY : renderPart(item, -0.3F, 0F, -0.3F, 0.6F); break;
		case EQUIPPED : renderPart(item, 0.0F, 0.15F, 0.0F, 1.0F); break;
		case EQUIPPED_FIRST_PERSON : renderPart(item, 1.0F, -0.2F, -0.4f, 2.0F); break;
		case INVENTORY : renderPart(item, 0.0F, 0.2F, 0.0F, 1.0F); break;
		default: break;
		}
	}
	
	private void renderPart(ItemStack stack, float x, float y, float z, float scale)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.setDynamic();
		CCRenderState.pullLightmap();
		prepareInv(stack);
		CCRenderState.startDrawing();
		renderStatic(new Scale(scale).with(new Translation(x, y, z)), 0);
		CCRenderState.draw();
		renderDynamic(new Scale(scale).with(new Translation(x, y, z)));
	}
}

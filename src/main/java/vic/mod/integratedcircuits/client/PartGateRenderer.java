package vic.mod.integratedcircuits.client;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import vic.mod.integratedcircuits.client.model.IComponentModel;
import vic.mod.integratedcircuits.client.model.ModelBase;
import vic.mod.integratedcircuits.client.model.ModelBundledConnection;
import vic.mod.integratedcircuits.client.model.ModelRedstoneConnection;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.item.ItemPartGate;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.Vertex5;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

import com.google.common.collect.Lists;

public class PartGateRenderer <T extends PartGate> implements IItemRenderer
{	
	protected List<IComponentModel> models = Lists.newLinkedList();
	protected ModelBase modelBase;
	private boolean isFMP;
	
	private ModelBundledConnection[] bundledModels = new ModelBundledConnection[4];
	private ModelRedstoneConnection[] redstoneModels = new ModelRedstoneConnection[4];
	
	public PartGateRenderer()
	{
		models.add(modelBase = new ModelBase());
	}
	
	public void addBundledConnections(int flag1, int... size)
	{
		if((flag1 & 1) != 0) bundledModels[0] = new ModelBundledConnection(0, size[0]);
		if((flag1 & 2) != 0) bundledModels[1] = new ModelBundledConnection(1, size[1]);
		if((flag1 & 4) != 0) bundledModels[2] = new ModelBundledConnection(2, size[2]);
		if((flag1 & 8) != 0) bundledModels[3] = new ModelBundledConnection(3, size[3]);
		
		for(IComponentModel m : bundledModels)
			if(m != null) models.add(m);
	}
	
	public void addRedstoneConnections(int flag1, int... size)
	{
		if((flag1 & 1) != 0) redstoneModels[3] = new ModelRedstoneConnection(0, size[0]);
		if((flag1 & 2) != 0) redstoneModels[0] = new ModelRedstoneConnection(1, size[1]);
		if((flag1 & 4) != 0) redstoneModels[1] = new ModelRedstoneConnection(2, size[2]);
		if((flag1 & 8) != 0) redstoneModels[2] = new ModelRedstoneConnection(3, size[3]);
		
		for(IComponentModel m : redstoneModels)
			if(m != null) models.add(m);
	}
	
	public void prepareBundled(int flag)
	{
		for(int i = 0; i < 4; i++)
		{
			if(bundledModels[i] == null) continue;
			bundledModels[i].rendered = (flag & 1) != 0;
			flag >>= 1;
		}
	}
	
	public void prepareRedstone(int flag1, int flag2)
	{
		for(int i = 0; i < 4; i++)
		{
			if(redstoneModels[i] == null) continue;
			redstoneModels[i].rendered = (flag1 & 1) != 0;
			redstoneModels[i].active = (flag2 & 1) != 0;
			flag1 >>= 1;
			flag2 >>= 1;
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
				m.normals()[i + 1] = m.normals()[i + 3];
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

	public void prepare(T part) 
	{
		isFMP = part.getProvider().isMultipart();
	}
	
	public void prepareInv(ItemStack stack)
	{
		isFMP = ((ItemPartGate)stack.getItem()).isMultipartItem();
	}
	
	public void prepareDynamic(T part, float partialTicks) {}
	
	public void renderStatic(Transformation t, int orient)
	{
		modelBase.setIsFMP(isFMP);
		for(IComponentModel m : models) m.renderModel(t, orient);
	}
	
	public void renderDynamic(Transformation t) {}
	
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

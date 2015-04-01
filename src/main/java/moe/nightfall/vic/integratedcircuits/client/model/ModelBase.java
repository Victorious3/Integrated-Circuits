package moe.nightfall.vic.integratedcircuits.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelBase implements IComponentModel
{
	public static CCModel[] models = ModelHelper.generate(generateModel(), 24);
	
	private String iconName;
	
	public ModelBase(String iconName) 
	{
		this.iconName = iconName;
	}

	@Override
	public void renderModel(Transformation t, int orient)
	{
		IIcon icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getTextureExtry(iconName);
		models[orient % 24].render(t, new IconTransformation(icon));
	}
	
	private static CCModel generateModel()
	{
		CCModel m1 = CCModel.quadModel(24);
		m1.generateBlock(0, 0, 0, 0, 1, 2 / 16D, 1);
		m1.computeNormals();
		ModelHelper.shrink(m1, 0.0002);
		return m1;
	}
}
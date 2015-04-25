package moe.nightfall.vic.integratedcircuits.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;

public class ModelBase implements IComponentModel {
	public static CCModel model = generateModel();

	private String iconName;

	public ModelBase(String iconName) {
		this.iconName = iconName;
	}

	@Override
	public void renderModel(Transformation t) {
		IIcon icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager()
			.getTexture(TextureMap.locationBlocksTexture)).getTextureExtry(iconName);
		model.copy().apply(t)
			.computeLighting(LightModel.standardLightModel)
			.render(new IconTransformation(icon));
	}

	private static CCModel generateModel() {
		CCModel m1 = CCModel.quadModel(24);
		m1.generateBlock(0, 0, 0, 0, 1, 2 / 16D, 1);
		m1.apply(new Scale(0.99));
		m1.computeNormals();
		return m1;
	}
}
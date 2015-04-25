package moe.nightfall.vic.integratedcircuits.client.model;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelChip implements IComponentModel {
	private static CCModel model = generateModel();

	@Override
	public void renderModel(Transformation arg0) {
		model.copy().apply(arg0)
			.computeLighting(LightModel.standardLightModel)
			.render(new IconTransformation(Resources.ICON_IC));
	}

	private static CCModel generateModel() {
		CCModel m1 = CCModel.quadModel(24);
		m1.generateBlock(0, 0, 0, 0, 12 / 16D, 3 / 16D, 12 / 16D);
		m1.computeNormals();
		return m1;
	}
}
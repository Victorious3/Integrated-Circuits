package vic.mod.integratedcircuits.client.model;

import vic.mod.integratedcircuits.Resources;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;

public class ModelChip implements IComponentModel
{
	private static CCModel[] models = new CCModel[24];
	private static CCModel base = generateModel();
	
	static
	{
		for(int i = 0; i < 24; i++) models[i] = PartCircuitRenderer.bakeCopy(base, i);
	}

	@Override
	public void renderModel(Transformation arg0, int arg1)
	{
		models[arg1 % 24].render(arg0, new IconTransformation(Resources.ICON_IC));
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
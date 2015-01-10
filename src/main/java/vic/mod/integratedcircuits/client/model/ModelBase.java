package vic.mod.integratedcircuits.client.model;

import vic.mod.integratedcircuits.client.PartGateRenderer;
import vic.mod.integratedcircuits.client.Resources;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelBase implements IComponentModel
{
	public static CCModel[] models = new CCModel[24];
	private static CCModel base = generateModel();
	
	static
	{
		for(int i = 0; i < 24; i++) models[i] = PartGateRenderer.bakeCopy(base, i);
	}
	
	private boolean isFMP;
	
	public void setIsFMP(boolean isFMP)
	{
		this.isFMP = isFMP;
	}

	@Override
	public void renderModel(Transformation t, int orient)
	{
		models[orient % 24].render(t, new IconTransformation(isFMP ? Resources.ICON_IC_BASE_FMP : Resources.ICON_IC_BASE));
	}
	
	private static CCModel generateModel()
	{
		CCModel m1 = CCModel.quadModel(24);
		m1.generateBlock(0, 0, 0, 0, 1, 2 / 16D, 1);
		m1.computeNormals();
		PartGateRenderer.shrink(m1, 0.0002);
		return m1;
	}
}
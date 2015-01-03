package vic.mod.integratedcircuits.client.model;

import vic.mod.integratedcircuits.client.PartRenderer;
import vic.mod.integratedcircuits.client.Resources;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelRedstoneConnection implements IComponentModel
{
	private CCModel[] conModels = new CCModel[24];
	private final int rotation;
	public boolean rendered = true;
	public boolean active = false;
	private int small;
	
	public ModelRedstoneConnection(int rotation, int size)
	{
		this.rotation = rotation;
		CCModel model = generateModel(size);
		for(int i = 0; i < 24; i++)
			conModels[i] = PartRenderer.bakeCopy(model, i).shrinkUVs(0.002);
	}

	@Override
	public void renderModel(Transformation t, int arg1)
	{	
		if(!rendered) return;
		arg1 = arg1 & 28 | ((arg1 + rotation) & 3);
		conModels[arg1 % 24].render(t, new IconTransformation(active ? Resources.ICON_IC_RSWIRE_ON : Resources.ICON_IC_RSWIRE_OFF));
	}
	
	private static CCModel generateModel(int size)
	{
		CCModel m1 = CCModel.quadModel(72);
		m1.generateBox(0, 0, 2, 7, size, 0.32, 2, 0, 0, 16, 16, 16);
		m1.generateBox(24, 0, 2, 6, size, 0.16, 1, 9, 0, 16, 16, 16);
		m1.generateBox(48, 0, 2, 9, size, 0.16, 1, 9, 0, 16, 16, 16);
		m1.computeNormals();
		return m1;
	}
}
package vic.mod.integratedcircuits.client.model;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.Resources;
import vic.mod.integratedcircuits.client.PartRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;

public class ModelBundledConnection implements IComponentModel
{
	private CCModel[] conModels = new CCModel[24];
	private final int rotation;
	public boolean rendered = true;
	
	public ModelBundledConnection(int rotation, int size)
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
		ForgeDirection dir = ForgeDirection.getOrientation((arg1 & 3) + 2);
		ForgeDirection dir1 = ForgeDirection.getOrientation((arg1 & 28) >> 2).getRotation(ForgeDirection.UP);
		boolean b = ((dir1.ordinal() % 2 == 0 ? 12 : 9) & dir.flag >> 2) > 0;
		conModels[arg1 % 24].render(t, new IconTransformation(b ? Resources.ICON_IC_WIRE_FLIPPED : Resources.ICON_IC_WIRE));
	}
	
	private CCModel generateModel(int size)
	{
		CCModel m1 = CCModel.quadModel(24);
		m1.generateBlock(0, 5 / 16D, 0.0003, 0, 11 / 16D, 4 / 16D, size / 16D);
		m1.computeNormals();
		return m1;
	}
}
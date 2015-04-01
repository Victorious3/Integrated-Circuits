package moe.nightfall.vic.integratedcircuits.client.model;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.Vertex5;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;

public class ModelHelper
{
	private ModelHelper() {}
	
	public static CCModel[] generate(CCModel base, int rotations) 
	{
		CCModel[] models = new CCModel[rotations];
		for(int i = 0; i < 24; i++) models[i] = genRotated(base, i);
		return models;
	}
	
	public static CCModel genRotated(CCModel base, int orient)
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
}

package vic.mod.integratedcircuits.ic.part.timed;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public interface IConfigurableDelay
{
	public int getConfigurableDelay(Vec2 pos, ICircuit parent);
	
	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay);
}
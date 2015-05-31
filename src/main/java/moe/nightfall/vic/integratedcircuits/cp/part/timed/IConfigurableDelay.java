package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;

public interface IConfigurableDelay {
	public int getConfigurableDelay(Vec2 pos, ICircuit parent);

	public void setConfigurableDelay(Vec2 pos, ICircuit parent, int delay);
}
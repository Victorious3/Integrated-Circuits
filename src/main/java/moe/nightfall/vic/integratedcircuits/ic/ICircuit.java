package moe.nightfall.vic.integratedcircuits.ic;

import net.minecraftforge.common.util.ForgeDirection;

public interface ICircuit extends ICircuitDataProvider {
	public boolean getInputFromSide(ForgeDirection dir, int frequency);

	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output);
}

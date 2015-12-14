package moe.nightfall.vic.integratedcircuits.cp;

import net.minecraft.util.EnumFacing;

public interface ICircuit extends ICircuitDataProvider {
	public boolean getInputFromSide(EnumFacing dir, int frequency);

	public void setOutputToSide(EnumFacing dir, int frequency, boolean output);
}

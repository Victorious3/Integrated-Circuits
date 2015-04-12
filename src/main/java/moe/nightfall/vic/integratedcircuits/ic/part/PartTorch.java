package moe.nightfall.vic.integratedcircuits.ic.part;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTorch extends CircuitPart
{
	@Override
	public Category getCategory() {
		return Category.TORCH;
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side)
	{
		return true;
	}
}

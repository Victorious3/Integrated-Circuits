package moe.nightfall.vic.integratedcircuits.ic.part.cell;

import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.ic.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartBufferCell extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		ForgeDirection dir = toInternal(pos, parent, side);
		getNeighbourOnSide(pos, parent, side.getOpposite()).onInputChange(pos.offset(side.getOpposite()), parent, side);
		markForUpdate(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection fd = toInternal(pos, parent, side);
		if (fd == ForgeDirection.EAST)
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.WEST));
		else if (fd == ForgeDirection.WEST)
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST));

		boolean out = super.getOutputToSide(pos, parent, side);
		if (fd == ForgeDirection.NORTH && !out)
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.SOUTH));
		else if (fd == ForgeDirection.SOUTH && !out)
			return getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.NORTH));

		return out;
	}

	@Override
	protected void calcOutput(Vec2 pos, ICircuit parent) {
		setOutput(
				pos,
				parent,
				(getInputFromSide(pos, parent, toExternal(pos, parent, ForgeDirection.EAST)) || getInputFromSide(pos,
						parent, toExternal(pos, parent, ForgeDirection.WEST))));
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) {
		return fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH;
	}
}

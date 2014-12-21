package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitPart;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.Vec2;

public class PartNull extends CircuitPart
{
	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) 
	{
		for(int i = 2; i < 6; i++)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			CircuitPart part = getNeighbourOnSide(pos, parent, fd);
			part.onInputChange(pos.offset(fd), parent, fd.getOpposite());
		}
	}
}
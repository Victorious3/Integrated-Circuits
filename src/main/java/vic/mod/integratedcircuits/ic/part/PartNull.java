package vic.mod.integratedcircuits.ic.part;

import vic.mod.integratedcircuits.ic.CircuitPart;
import net.minecraftforge.common.util.ForgeDirection;

public class PartNull extends CircuitPart
{
	@Override
	public void onPlaced() 
	{
		for(int i = 2; i < 6; i++)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(i);
			CircuitPart part = getNeighbourOnSide(fd);
			part.onInputChange(fd.getOpposite());
		}
	}
}
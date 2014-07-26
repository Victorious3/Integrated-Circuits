package vic.mod.integratedcircuits;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

public class Content implements IPartFactory
{
	@Override
	public TMultiPart createPart(String arg0, boolean arg1) 
	{
		if(arg0.equals(IntegratedCircuits.partCircuit)) return new PartCircuit();
		return null;
	}
	
	//TODO Move it, it does not fit in here!
	
	public static ForgeDirection[] order = new ForgeDirection[]{NORTH, EAST, SOUTH, WEST};
	public static int[] index = new int[]{-1, -1, 0, 2, 3, 1, -1};
	
	public static ForgeDirection rot(ForgeDirection fd)
	{
		return rotn(fd, 1);
	}
	
	public static ForgeDirection rotn(ForgeDirection fd, int offset)
	{
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}
}

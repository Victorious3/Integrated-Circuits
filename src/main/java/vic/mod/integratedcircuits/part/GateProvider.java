package vic.mod.integratedcircuits.part;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class GateProvider
{
	private GateProvider() {}
	
	public interface IGateProvider 
	{
		public void markRender();
		
		public MCDataOutput getWriteStream(int disc);
		
		public World getWorld();
		
		public void notifyBlocksAndChanges();
		
		public void notifyPartChange();
		
		public BlockCoord getPos();
		
		public TileEntity getTileEntity();
		
		public void destroy();
		
		public int updateRedstoneInput(int side);
		
		public byte[] updateBundledInput(int side);
		
		public void scheduleTick(int delay);
		
		public PartGate getGate();
	}
	
	public static PartGate getGateAt(World world, BlockCoord pos, int side)
	{
		TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
		if(te instanceof TileMultipart)
		{
			TileMultipart tm = (TileMultipart)te;
			TMultiPart multipart = tm.partMap(side);
			if(multipart instanceof IGateProvider) 
				return ((IGateProvider)multipart).getGate();
		}
		return null;
	}
}

package vic.mod.integratedcircuits.gate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

public interface ISocket
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
	
	public void setGate(PartGate gate);
	
	public PartGate getGate();
	
	public int strongPowerLevel(int side);
}
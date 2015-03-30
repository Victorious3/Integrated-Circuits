package vic.mod.integratedcircuits.gate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;

interface ISocketBridge
{
	public void markRender();
	
	public MCDataOutput getWriteStream(int disc);
	
	public World getWorld();
	
	public void notifyBlocksAndChanges();
	
	public void notifyPartChange();
	
	public BlockCoord getPos();
	
	public TileEntity getTileEntity();
	
	public void destroy();
	
	public void updateInput();
	
	public int updateRedstoneInput(int side);
	
	public byte[] updateBundledInput(int side);
	
	public void scheduleTick(int delay);
	
	public int strongPowerLevel(int side);
	
	static interface ISocketBase extends ISocketBridge
	{
		public void setGate(IGate gate);
		
		public IGate getGate();
		
		public int getSide();
		
		public int getSideRel(int side);
		
		public void setSide(int side);
		
		public int getRotation();
		
		public int getRotationAbs(int rel);
		
		public int getRotationRel(int abs);
		
		public void setRotation(int rot);
		
		public byte[][] getInput();
		
		public byte[][] getOutput();
		
		public void setInput(byte[][] input);
		
		public void setOutput(byte[][] output);
		
		public byte getRedstoneInput(int side);
		
		public byte getBundledInput(int side, int frequency);
		
		public byte getRedstoneOutput(int side);

		public byte getBundledOutput(int side, int frequency);
		
		public void setInput(int side, int frequency, byte input);
		
		public void setOutput(int side, int frequency, byte output);
		
		public void resetInput();
		
		public void resetOutput();
	}
}

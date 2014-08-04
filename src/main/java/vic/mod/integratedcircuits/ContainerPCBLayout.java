package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.ForgeDirection;

public class ContainerPCBLayout extends Container implements ICircuit
{
	private TileEntityPCBLayout tileentity;
	
	public ContainerPCBLayout(TileEntityPCBLayout tileentity)
	{
		this.tileentity = tileentity;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return player.getDistanceSq(tileentity.xCoord + 0.5, tileentity.yCoord + 0.5, tileentity.zCoord + 0.5) < 64;
	}

	@Override
	public int[][][] getMatrix() 
	{
		return tileentity.pcbMatrix;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		// TODO Auto-generated method stub	
	}
}

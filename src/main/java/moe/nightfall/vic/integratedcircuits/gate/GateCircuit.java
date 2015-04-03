package moe.nightfall.vic.integratedcircuits.gate;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IGatePeripheralProvider;
import moe.nightfall.vic.integratedcircuits.api.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.ic.CircuitData;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;

public class GateCircuit extends Gate implements ICircuit, IGatePeripheralProvider
{
	public CircuitData circuitData;

	private boolean update;
	private CircuitPeripheral peripheral = new CircuitPeripheral(this);
	
	@Override
	public void preparePlacement(EntityPlayer player, ItemStack stack)
	{
		NBTTagCompound comp = stack.stackTagCompound;
		if(comp == null) return;
		
		circuitData = CircuitData.readFromNBT(comp.getCompoundTag("circuit"), this);
		circuitData.setQueueEnabled(false);
	}

	@Override
	public void load(NBTTagCompound tag) 
	{
		super.load(tag);
		circuitData = CircuitData.readFromNBT(tag.getCompoundTag("circuit"), this);
		circuitData.setQueueEnabled(false);
	}
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		super.save(tag);
		tag.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readDesc(MCDataInput packet) 
	{
		super.readDesc(packet);
		circuitData = CircuitData.readFromNBT(packet.readNBTTagCompound(), this);
		circuitData.setQueueEnabled(false);
	}
	
	@Override
	public void writeDesc(MCDataOutput packet) 
	{
		super.writeDesc(packet);
		packet.writeNBTTagCompound(circuitData.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public ItemStack getItemStack()
	{
		ItemStack stack = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		stack.stackTagCompound = comp;
		return stack;
	}
	
	@Override
	public CircuitData getCircuitData() 
	{
		return circuitData;
	}
	
	@Override
	public void onAdded() 
	{
		if(!provider.getWorld().isRemote)
		{
			provider.updateInput();
			circuitData.updateInput();
			circuitData.updateOutput();
		}
	}

	@Override
	public void updateInputPost() 
	{
		super.updateInputPost();
		provider.scheduleTick(0);
	}

	@Override
	public void scheduledTick() 
	{
		circuitData.updateInput();
	}

	@Override
	public void update() 
	{
		if(!provider.getWorld().isRemote)
			circuitData.updateMatrix();
	}

	// TODO Re-implement comparator input
	/*public boolean hasComparatorInput(int side)
	{
		int r = getRotationAbs(side);
		int abs = Rotation.rotateSide(getSide(), r);

		BlockCoord pos = provider.getPos().offset(abs);
		Block b = provider.getWorld().getBlock(pos.x, pos.y, pos.z);
		return b.hasComparatorInputOverride();
	}

	public int updateComparatorInput(int side)
	{
		int r = getRotationAbs(side);
		int abs = Rotation.rotateSide(getSide(), r);

		BlockCoord pos = provider.getPos().offset(abs);
		Block b = provider.getWorld().getBlock(pos.x, pos.y, pos.z);
		if(b != null && b.hasComparatorInputOverride())
			return b.getComparatorInputOverride(provider.getWorld(), pos.x, pos.y, pos.z, abs ^ 1);
		return 0;
	}*/

	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		if(getConnectionTypeAtSide(side) == EnumConnectionType.ANALOG)
			return provider.getRedstoneInput(side) == frequency;
		return provider.getBundledInput(side, frequency) != 0;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		EnumConnectionType mode = getConnectionTypeAtSide(side);
		if(mode == EnumConnectionType.SIMPLE && frequency > 0) return;	

		provider.setOutput(side, frequency, (byte)(output ? (mode == EnumConnectionType.BUNDLED ? -1 : 15) : 0));
		provider.notifyBlocksAndChanges();
	}

	@Override
	public boolean hasPeripheral(int side) 
	{
		return true;
	}

	@Override
	public GatePeripheral getPeripheral() 
	{
		return peripheral;
	}

	@Override
	public Cuboid6 getDimension()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumConnectionType getConnectionTypeAtSide(int side)
	{
		return circuitData.getProperties().getModeAtSide((side + 2) % 4);
	}
	
	@Override
	public boolean hasComparatorInputAtSide(int side)
	{
		return getConnectionTypeAtSide(side) == EnumConnectionType.ANALOG;
	}
}

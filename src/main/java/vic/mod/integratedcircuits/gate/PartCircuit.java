package vic.mod.integratedcircuits.gate;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.CircuitProperties;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCircuit extends PartGate implements ICircuit, IGatePeripheralProvider
{
	public CircuitData circuitData;

	private boolean update;
	private CircuitPeripheral peripheral = new CircuitPeripheral(this);
		
	public PartCircuit() 
	{
		super("circuit");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public PartCircuitRenderer getRenderer() 
	{
		return ClientProxy.circuitRenderer;
	}
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
	{
		super.preparePlacement(player, pos, side, meta);
		
		ItemStack stack = player.getCurrentEquippedItem();
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
		ItemStack stack = new ItemStack(IntegratedCircuits.item7Segment);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		stack.stackTagCompound = comp;
		return stack;
	}

	private int getModeAtSide(int s)
	{
		return circuitData.getProperties().getModeAtSide((s + 2) % 4);
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
	public int updateRedstoneInput(int side) 
	{
		int oin = input[side][0];
		int in = super.updateRedstoneInput(side);
		if(in == 0 && getModeAtSide(side) == CircuitProperties.ANALOG)
			in = updateComparatorInput(side);
		return in;
	}

	@Override
	public void update() 
	{
		if(!provider.getWorld().isRemote)
		{
			for(int i = 0; i < 4; i++)
				if(getModeAtSide(i) == CircuitProperties.ANALOG && hasComparatorInput(i))
				{
					int in = (byte)updateComparatorInput(i);
					if(in != input[i][0]) 
					{
						input[i][0] = (byte)in;
						provider.scheduleTick(0);
					}
				}
			circuitData.updateMatrix();
		}
	}
	
	@Override
	public byte getRedstoneOutput(int side) 
	{
		//TODO Move over to PartGate!
		if(getModeAtSide(side) == CircuitProperties.ANALOG)
		{
			byte[] out = output[side];
			for(byte i = 15; i >= 0; i--)
				if(out[i] != 0) return i;
		}
		return super.getRedstoneOutput(side);
	}

	public boolean hasComparatorInput(int side)
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
	}

	@Override
	public boolean canConnectRedstone(int arg0) 
	{
		return getModeAtSide(arg0) != CircuitProperties.BUNDLED;
	}
	
	@Override
	public boolean canConnectBundledl(int arg0) 
	{
		return getModeAtSide(arg0) == CircuitProperties.BUNDLED;
	}

	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		if(getModeAtSide(side) == CircuitProperties.ANALOG)
			return input[side][0] == frequency && getRedstoneOutput(side) == 0;
		return input[side][frequency] != 0 && output[side][frequency] == 0;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		int mode = getModeAtSide(side);
		if(mode == CircuitProperties.SIMPLE && frequency > 0) return;	

		this.output[side][frequency] = (byte)(output ? (mode == CircuitProperties.BUNDLED ? -1 : 15) : 0);
		provider.notifyBlocksAndChanges();
		
		updateRedstoneIO();
	}

	@Override
	public PartGate newInstance() 
	{
		return new PartCircuit();
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
}

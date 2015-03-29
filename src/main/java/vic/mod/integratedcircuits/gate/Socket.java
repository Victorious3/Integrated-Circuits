package vic.mod.integratedcircuits.gate;

import io.netty.buffer.Unpooled;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.item.IGateItem;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;

public class Socket implements ISocket
{
	//Collision box
	public static Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);
		
	protected final ISocket provider;
	protected PartGate gate;
	
	//Used by the client, redstone IO
	protected byte io;
	protected byte[][] output = new byte[4][16];
	protected byte[][] input = new byte[4][16];
	
	protected byte orientation;
	
	private Socket(ISocket provider) 
	{
		this.provider = provider;
	}
	
	//Bridge methods
	
	public void update()
	{
		if(gate != null) gate.update();
	}
	
	public void onAdded()
	{
		if(gate != null) gate.onAdded();
	}
	
	public void onMoved()
	{
		if(gate != null) gate.onMoved();
	}
	
	//Bridge methods, calling liked IGateProvider
	
	@Override
	public void markRender()
	{
		provider.markRender();
	}

	@Override
	public MCDataOutput getWriteStream(int disc)
	{
		return provider.getWriteStream(disc);
	}

	@Override
	public World getWorld()
	{
		return provider.getWorld();
	}

	@Override
	public void notifyBlocksAndChanges()
	{
		provider.notifyBlocksAndChanges();
	}

	@Override
	public void notifyPartChange()
	{
		provider.notifyPartChange();
	}

	@Override
	public BlockCoord getPos()
	{
		return provider.getPos();
	}

	@Override
	public TileEntity getTileEntity()
	{
		return provider.getTileEntity();
	}

	@Override
	public void destroy()
	{
		if(gate != null) 
		{
			BlockCoord pos = getPos();
			MiscUtils.dropItem(getWorld(), gate.getItemStack(), pos.x, pos.y, pos.z);
		}
		provider.destroy();
	}

	@Override
	public int updateRedstoneInput(int side)
	{
		return provider.updateRedstoneInput(side);
	}

	@Override
	public byte[] updateBundledInput(int side)
	{
		return provider.updateBundledInput(side);
	}

	@Override
	public void scheduleTick(int delay)
	{
		provider.scheduleTick(delay);
	}
	
	@Override
	public void setGate(PartGate gate)
	{
		this.gate = gate;
		this.gate.setProvider(this);
		this.gate.onAdded();
	}

	@Override
	public PartGate getGate()
	{
		return gate;
	}

	@Override
	public int strongPowerLevel(int side)
	{
		return provider.strongPowerLevel(side);
	}
	
	//IO
	
	public void readFromNBT(NBTTagCompound compound)
	{
		// Read orientation and IO
		orientation = compound.getByte("orientation");
		io = compound.getByte("io");
		
		byte[] input = compound.getByteArray("input");
		byte[] output = compound.getByteArray("output");
		
		for(int i = 0; i < 4; i++)
		{
			this.input[i] = Arrays.copyOfRange(input, i * 16, (i + 1) * 16);
			this.output[i] = Arrays.copyOfRange(output, i * 16, (i + 1) * 16);
		}
		
		// Read gate from NBT, if present
		if(compound.hasKey("gate_id")) 
		{
    		gate = GateRegistry.createGateInstace(compound.getString("gate_id"));
    		gate.setProvider(this);
    		gate.load(compound.getCompoundTag("gate"));
		}
	}
	
	public void writeToNBT(NBTTagCompound compound)
	{
		// Write orientation and IO
		compound.setByte("orientation", orientation);
		compound.setByte("io", io);
		
		byte[] input = null;
		byte[] output = null;
		
		for(int i = 0; i < 4; i++)
		{
			input = ArrayUtils.addAll(input, this.input[i]);
			output = ArrayUtils.addAll(output, this.output[i]);
		}
		
		compound.setByteArray("input", input);
		compound.setByteArray("output", output);
		
		// Write gate to NBT, if present
		if(gate != null) 
		{
			compound.setString("gate_id", gate.getName());
			NBTTagCompound gateCompound = new NBTTagCompound();
			gate.save(gateCompound);
			compound.setTag("gate", gateCompound);
		}
	}
	
	private void writeDesc(MCDataOutput packet)
	{
		if(gate != null) gate.writeDesc(packet);
	}
	
	private void readDesc(MCDataInput packet)
	{
		if(gate != null) gate.readDesc(packet);
	}
	
	public void writeDesc(NBTTagCompound compound)
	{
		compound.setByte("orientation", orientation);
		compound.setByte("io", io);
		
		if(gate != null) 
		{
			PacketCustom packet = new PacketCustom("", 1);
			gate.writeDesc(packet);
			compound.setString("gate_id", gate.getName());
			compound.setByteArray("data", packet.getByteBuf().array());
		}
	}
	
	public void readDesc(NBTTagCompound compound)
	{
		orientation = compound.getByte("orientation");
		io = compound.getByte("io");
		
		if(compound.hasKey("gate_id"))
		{
			byte[] data = compound.getByteArray("data");
			PacketCustom in = new PacketCustom(Unpooled.copiedBuffer(data));
			
			gate = GateRegistry.createGateInstace(compound.getString("gate_id"));
			gate.setProvider(this);
			gate.readDesc(in);
		}
	}
	
	public void read(MCDataInput packet)
	{
		byte discr = packet.readByte();
		switch (discr) {
		case 0 :
			orientation = packet.readByte();
			markRender();
			return;
		case 1 :
			io = packet.readByte();
			markRender();
			return;
		}
		if(gate != null) gate.read(discr, packet);
	}
	
	//Rotation
	
	public int getSide()
	{
		return orientation >> 2;
	}
	
	public int getSideRel(int side)
	{
		return getRotationRel(Rotation.rotationTo(getSide(), side));
	}
	
	public void setSide(int s)
	{
		orientation = (byte)(orientation & 3 | s << 2);
	}
	
	public int getRotation()
	{
		return orientation & 3;
	}
	
	public int getRotationAbs(int rel)
	{
		return (rel + getRotation() + 2) % 4;
	}
	
	public int getRotationRel(int abs)
	{
		return (abs + 6 - getRotation()) % 4;
	}
	
	public void setRotation(int r)
	{
		orientation = (byte)(orientation & 252 | r);
	}
	
	//Redstone IO
	
	public byte getRedstoneInput(int side)
	{
		return getBundledInput(side, 0);
	}
	
	public byte[] getBundledInput(int side) 
	{
		return input[side];
	}
	
	public byte getBundledInput(int side, int frequency)
	{
		return input[side][frequency];
	}
	
	public byte getRedstoneOutput(int side) 
	{
		return getBundledOutput(side, 0);
	}
	
	public byte[] getBundledOutput(int side) 
	{
		return output[side];
	}

	public byte getBundledOutput(int side, int frequency) 
	{
		return output[side][frequency];
	}
	
	public void setInput(byte[][] input) 
	{
		this.input = input;
	}
	
	public void setOutput(byte[][] output)
	{
		this.output = output;
	}
	
	public void setInput(int side, byte[] input) 
	{
		this.input[side] = input;
	}
	
	public void setOutput(int side, byte[] output)
	{
		this.output[side] = output;
	}
	
	public void setInput(int side, int frequency, byte input) 
	{
		this.input[side][frequency] = input;
	}
	
	public void setOutput(int side, int frequency, byte output) 
	{
		this.output[side][frequency] = output;
	}
	
	public void resetInput()
	{
		this.input = new byte[4][16];
	}
	
	public void resetOutput()
	{
		this.output = new byte[4][16];
	}
	
	public void updateInput()
	{
		updateInputPre();
		for(int i = 0; i < 4; i++)
		{
			if(canConnectRedstone(i)) input[i][0] = (byte) updateRedstoneInput(i);
			else if(canConnectBundled(i)) input[i] = updateBundledInput(i);
		}
		updateInputPost();
	}
	
	public void updateInputPre()
	{
		if(gate != null) gate.updateInputPre();
	}
	
	public void updateInputPost()
	{
		if(gate != null) 
		{
			gate.updateInputPost();
			updateRedstoneIO();
		}
	}
	
	public boolean canConnectRedstone(int side)
	{
		return gate != null ? gate.canConnectRedstone(side) : false;
	}
	
	public boolean canConnectBundled(int side)
	{
		return gate != null ? gate.canConnectBundled(side) : false;
	}
	
	public void updateRedstoneIO()
	{
		byte oio = io;
		io = 0;
		for(int i = 0; i < 4; i++)
			io |= (getRedstoneInput(i) != 0 || getRedstoneOutput(i) != 0) ? 1 << i: 0;
		
		if(oio != io) provider.getWriteStream(1).writeByte(io);
	}
	
	//Interaction
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
	{
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
	}
	
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack stack)
	{
		if(stack != null)
		{
			if(gate == null && stack.getItem() instanceof IGateItem) 
			{
				gate = GateRegistry.createGateInstace(((IGateItem)stack.getItem()).getGateID(stack, player, getPos()));
				return true;
			}
			
			String name = stack.getItem().getUnlocalizedName();
			if(stack.getItem() == IntegratedCircuits.itemScrewdriver || name.equals("item.redlogic.screwdriver") || name.equals("item.bluepower:screwdriver") || name.equals("item.projectred.core.screwdriver"))
			{
				if(!getWorld().isRemote && gate != null) 
				{
					if(!player.isSneaking()) rotate();
					gate.onActivatedWithScrewdriver(player, hit, stack);
				}
				
				stack.damageItem(1, player);
				return true;
			}
		}	
		if(gate != null) return gate.activate(player, hit, stack);
		return false;
	}
	
	private void rotate()
	{
		setRotation((getRotation() + 1) % 4);
		getWriteStream(0).writeByte(orientation);
		notifyBlocksAndChanges();
		if(gate != null)
		{
			gate.onRotated();
			updateInput();
		}
	}
	
	public void onNeighborChanged()
	{
		if(!getWorld().isRemote) 
		{
			BlockCoord pos = getPos().offset(getSide());
			if(!MiscUtils.canPlaceGateOnSide(getWorld(), pos.x, pos.y, pos.z, getSide() ^ 1))
			{
				destroy();
			}
			else updateInput();
		}
		if(gate != null) gate.onNeighborChanged();
	}
	
	public Transformation getRotationTransformation()
	{
		return Rotation.sideOrientation(getSide(), getRotation()).at(Vector3.center);
	}
}

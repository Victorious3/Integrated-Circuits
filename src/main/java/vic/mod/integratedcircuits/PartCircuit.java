package vic.mod.integratedcircuits;

import java.util.Random;

import mrtjp.projectred.integration.BundledGateLogic;
import mrtjp.projectred.integration.BundledGatePart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.util.MiscUtils;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCircuit extends BundledGatePart implements ICircuit
{
	public byte tier;
	public String name;
	public byte[][] output = new byte[4][16];
	public byte[][] input = new byte[4][16];
	
	public CircuitData circuitData;
	
	@Override
	public String getType() 
	{
		return IntegratedCircuits.partCircuit;
	}
	
	@Override
    public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
    {
		subID = (byte)meta;
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
		logic = new CircuitLogic(this);
		
		ItemStack stack = player.getCurrentEquippedItem();
		NBTTagCompound comp = stack.stackTagCompound;
		if(comp == null) return;
		
		state = comp.getByte("con");
		tier = comp.getByte("tier");
		name = comp.getString("name");
		circuitData = CircuitData.readFromNBT(comp.getCompoundTag("circuit"), this);
    }

	@Override
	public void load(NBTTagCompound tag) 
	{
		orientation = tag.getByte("orient");
		subID = tag.getByte("subID");
		shape = tag.getByte("shape");
		connMap = tag.getShort("connMap") & 0xFFFF;
		schedTime = tag.getLong("schedTime");
		state = tag.getByte("state");
		logic = new CircuitLogic(this);
		logic.load(tag);
		
		//My part
		tier = tag.getByte("tier");
		name = tag.getString("name");
		circuitData = CircuitData.readFromNBT(tag.getCompoundTag("circuit"), this);
	}
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		super.save(tag);
		
		tag.setShort("tier", tier);
		tag.setString("name", name);
		tag.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readDesc(MCDataInput packet) 
	{
		orientation = packet.readByte();
		subID = packet.readByte();
		shape = packet.readByte();
		state = packet.readByte();
		if(logic == null) logic = new CircuitLogic(this);
		logic.readDesc(packet);
		
		//My part
		tier = packet.readByte();
		name = packet.readString();
		circuitData = CircuitData.readFromNBT(packet.readNBTTagCompound(), this);
	}
	
	@Override
	public ItemStack getItem() 
	{
		ItemStack stack = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		comp.setInteger("con", state);
		comp.setString("name", name);
		stack.stackTagCompound = comp;
		return stack;
	}

	@Override
	public void writeDesc(MCDataOutput packet) 
	{
		super.writeDesc(packet);
		
		packet.writeByte(tier);
		packet.writeString(name);
		packet.writeNBTTagCompound(circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(Random rand) 
	{
		
	}
	
	@Override
	public int getLightValue() 
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			CCRenderState.setBrightness(world(), x(), y(), z());
			ItemCircuit.renderer.prepare(this);
			ItemCircuit.renderer.renderStatic(pos.translation(), orientation & 0xFF);
			return true;
		}	
		else return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			ItemCircuit.renderer.prepareDynamic(this, frame);
			ItemCircuit.renderer.renderDynamic(this.rotationT().with(pos.translation()));
		}	
	}

	public class CircuitLogic extends BundledGateLogic
	{	
		public CircuitLogic(BundledGatePart gate) 
		{
			super(gate);
		}

		@Override
		public boolean canConnectBundled(BundledGatePart gate, int r) 
		{
			return isBundeledAtSide(r);
		}

		@Override
		public boolean canConnect(int shape, int r) 
		{
			return !isBundeledAtSide(r);
		}

		@Override
		public int getOutput(BundledGatePart gate, int r)
		{
			return isBundeledAtSide(r) ? 0 : output[r][0];
		}

		@Override
		public int outputMask(int shape) 
		{
			return 0xF;
		}

		@Override
		public int inputMask(int shape)
		{
			return 0xF;
		}

		@Override
		public void onChange(BundledGatePart gate) 
		{
			int in = getInput(gate, 15);
			for(int i = 0; i < 4; i++)
			{
				if(!isBundeledAtSide(i)) input[i][0] = (byte)((in >> i & 1) > 0 ? 15 : 0);
			}
			for(int i = 0; i < 4; i++)
			{
				if(!isBundeledAtSide(i)) continue;
				byte[] bin = getBundledInput(i);
				if(bin == null) bin = new byte[16];
				input[i] = bin;
			}
			circuitData.updateInput();
		}

		@Override
		public byte[] getBundledOutput(BundledGatePart gate, int r) 
		{
			return isBundeledAtSide(r) ? output[r] : null;
		}

		@Override
		public void onTick(BundledGatePart gate) 
		{
			if(gate.world().isRemote) return;
			circuitData.setQueueEnabled(false);
			circuitData.updateMatrix();
		}

		@Override
		public void read(MCDataInput packet, int switch_key) 
		{
			super.read(packet, switch_key);
		}
	}
	
	@Override
	public byte[] getBundledSignal(int r) 
	{
		return getLogic().getBundledOutput(this, toInternal(r));
	}

	private boolean isBundeledAtSide(int s)
	{
		return ((state >> MiscUtils.rotn(s, 2, 4)) & 1) != 0;
	}
	
	@Override
	public CircuitData getCircuitData() 
	{
		return circuitData;
	}
	
	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		int side = MiscUtils.getSide(MiscUtils.rotn(dir, 2));
		return input[side][frequency] != 0 && !(output[side][frequency] != 0);
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		int side = MiscUtils.getSide(MiscUtils.rotn(dir, 2));
		if(!isBundeledAtSide(side) && frequency > 0) return;
		byte oldOut = this.output[side][frequency];
		byte newOut = (byte)(output ? 15 : 0);
		this.output[side][frequency] = newOut;
		if(oldOut != newOut)
		{
			tile().markDirty();
			notifyNeighbors(15);
		}	
	}
}

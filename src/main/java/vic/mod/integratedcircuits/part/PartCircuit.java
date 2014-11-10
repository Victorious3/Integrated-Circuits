package vic.mod.integratedcircuits.part;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.util.MiscUtils;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCircuit extends GatePart implements ICircuit
{
	public byte tier, con;
	public String name;
	
	public CircuitData circuitData;
	
	@Override
	public String getType() 
	{
		return IntegratedCircuits.partCircuit;
	}
	
    public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
    {
		super.preparePlacement(player, pos, side, meta);
		
		ItemStack stack = player.getCurrentEquippedItem();
		NBTTagCompound comp = stack.stackTagCompound;
		if(comp == null) return;
		
		con = comp.getByte("con");
		tier = comp.getByte("tier");
		name = comp.getString("name");
		circuitData = CircuitData.readFromNBT(comp.getCompoundTag("circuit"), this);
    }

	@Override
	public void load(NBTTagCompound tag) 
	{
		super.load(tag);
		con = tag.getByte("con");
		tier = tag.getByte("tier");
		name = tag.getString("name");
		circuitData = CircuitData.readFromNBT(tag.getCompoundTag("circuit"), this);
	}
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		super.save(tag);
		tag.setByte("con", con);
		tag.setShort("tier", tier);
		tag.setString("name", name);
		tag.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readDesc(MCDataInput packet) 
	{
		super.readDesc(packet);
		con = packet.readByte();
		tier = packet.readByte();
		name = packet.readString();
		circuitData = CircuitData.readFromNBT(packet.readNBTTagCompound(), this);
	}
	
	@Override
	public void writeDesc(MCDataOutput packet) 
	{
		super.writeDesc(packet);
		packet.writeByte(con);
		packet.writeByte(tier);
		packet.writeString(name);
		packet.writeNBTTagCompound(circuitData.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public Iterable<ItemStack> getDrops() 
	{
		ItemStack stack = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		comp.setInteger("con", con);
		comp.setString("name", name);
		stack.stackTagCompound = comp;
		return Arrays.asList(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			CCRenderState.setBrightness(world(), x(), y(), z());
			ClientProxy.renderer.prepare(this);
			ClientProxy.renderer.renderStatic(pos.translation(), orientation & 255);
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
			ClientProxy.renderer.prepareDynamic(this, frame);
			ClientProxy.renderer.renderDynamic(this.getRotationTransformation().with(pos.translation()));
		}	
	}

	@Override
	public void onAdded() 
	{
		if(!world().isRemote)
		{
//			circuitData.updateInput();
//			circuitData.updateOutput();
		}
	}
	
	@Override
	public void onWorldJoin() 
	{
		onAdded();
	}

	private boolean isBundeledAtSide(int s)
	{
		return ((con >> ((s + 2) % 4)) & 1) != 0;
	}
	
	@Override
	public CircuitData getCircuitData() 
	{
		return circuitData;
	}
	
	@Override
	public boolean canConnectRedstoneImpl(int arg0) 
	{
		return !isBundeledAtSide(arg0);
	}
	
	@Override
	public boolean canConnectBundledImpl(int arg0) 
	{
		return isBundeledAtSide(arg0);
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
			tile().notifyTileChange();
			tile().notifyNeighborChange(getSide());
		}
	}
}

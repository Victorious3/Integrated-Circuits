package vic.mod.integratedcircuits.part;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.CircuitProperties;
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
	public CircuitData circuitData;
	private boolean update;
	
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
	
	public ItemStack getItem()
	{
		ItemStack stack = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		stack.stackTagCompound = comp;
		return stack;
	}
	
	@Override
	public Iterable<ItemStack> getDrops() 
	{
		return Arrays.asList(getItem());
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return getItem();
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
		super.onAdded();
		circuitData.updateOutput();
	}

	@Override
	public void updateInput() 
	{
		super.updateInput();
		scheduleTick(0);
	}

	@Override
	public void scheduledTick() 
	{	
		update = false;
		System.out.println("update...");
		circuitData.updateInput();
		if(update)
		{
			System.out.println("update!");
			tile().notifyPartChange(this);
			tile().notifyNeighborChange(getSide());
		}
	}

	@Override
	public int strongPowerLevel(int arg0) 
	{
		if((arg0 & 6) == (getSide() & 6)) return 0;
		int rot = getSideRel(arg0);
		int o = 0;
		
		if(!canConnectRedstoneImpl(rot)) return 0;
		if(getModeAtSide(rot) == CircuitProperties.ANALOG)
		{
			byte[] out = output[rot];
			for(int i = 15; i >= 0; i--)
				if(out[i] != 0) 
				{
					o = i;
					break;
				}
		}
		else o = output[rot][0];
		System.out.println(rot + " " + input[rot][0] + " " + o);
		return o;
	}

	@Override
	public boolean canConnectRedstoneImpl(int arg0) 
	{
		return getModeAtSide(arg0) != CircuitProperties.BUNDLED;
	}
	
	@Override
	public boolean canConnectBundledImpl(int arg0) 
	{
		return getModeAtSide(arg0) == CircuitProperties.BUNDLED;
	}

	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}

	@Override
	public void update() 
	{
		if(!world().isRemote) circuitData.updateMatrix();
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		if(getModeAtSide(side) == CircuitProperties.ANALOG)
			return input[side][0] == frequency && output[side][0] == 0;
		return input[side][frequency] != 0 && output[side][frequency] == 0;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		int side = (MiscUtils.getSide(dir) + 2) % 4;
		int mode = getModeAtSide(side);
		if(mode == CircuitProperties.SIMPLE && frequency > 0) return;
		else if(mode == CircuitProperties.ANALOG && this.input[side][0] != 0) return;
		this.output[side][frequency] = (byte)(output ? 15 : 0);
		System.out.println(mode + " " + side + " " + output + " " + frequency);
		update = true;
	}
}

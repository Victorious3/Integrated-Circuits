package vic.mod.integratedcircuits.part;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Part7Segment extends PartGate
{
	public int display = NUMBERS[0];
	public boolean isSlave;
	public BlockCoord parent;
	public ArrayList<BlockCoord> slaves = Lists.newArrayList();
	
	//    0
	//    --
	//  5|6 |1
	//    --
	//  4|3 |2
	//    --    #7
	public static final byte[] NUMBERS = {63, 6, 91, 79, 102, 109, 125, 7, 127, 111};
	public static final int DOT = 1 << 8;
	
	public Part7Segment() 
	{
		super("7segment");
	}

	@Override
	public void onAdded() 
	{
		super.onAdded();

		int abs = Rotation.rotateSide(getSide(), getRotationAbs(3));
		BlockCoord pos = new BlockCoord(tile());
		BlockCoord pos2 = pos.copy();
		Part7Segment seg;
		do {
			pos2.offset(abs);
			seg = getSegment(pos2);
			if(seg == null) break;
			if(seg.isSlave) continue;
			parent = pos2;
			isSlave = true;
			
			seg.claimSlaves();			
			seg.tile().markRender();
			seg.tile().notifyPartChange(seg);
			seg.updateSlaves();
			
			break;
		} while (true);
	}

	@Override
	public void onRemoved() 
	{
		super.onRemoved();
		if(!world().isRemote) updateConnections(false);
	}
	
	@Override
	public void rotate() 
	{
		super.rotate();
		updateConnections(true);
	}
	
	public void updateConnections(boolean update)
	{
		if(isSlave)
		{
			BlockCoord crd = new BlockCoord(tile());
			isSlave = false;
			if(update) claimSlaves();
			
			Part7Segment master = getSegment(parent);
			if(master != null) master.claimSlaves();
			int abs = Rotation.rotateSide(getSide(), getRotationAbs(1));
			crd.offset(abs);
			Part7Segment seg = getSegment(crd);
			if(seg != null) seg.claimSlaves();
		}
		else
		{
			int abs = Rotation.rotateSide(getSide(), getRotationAbs(1));
			BlockCoord crd = new BlockCoord(tile()).offset(abs);
			if(slaves.contains(crd))
			{
				Part7Segment seg = getSegment(crd);
				if(seg != null) seg.claimSlaves();
			}
			slaves.clear();
		}
	}

	public void claimSlaves()
	{
		isSlave = false;
		slaves.clear();
		int abs = Rotation.rotateSide(getSide(), getRotationAbs(1));
		BlockCoord pos = new BlockCoord(tile());
		BlockCoord pos2 = pos.copy();
		Part7Segment seg;
		do {
			pos2.offset(abs);
			seg = getSegment(pos2);
			if(seg == null) break;
			if(seg.isSlave) 
				slaves.add(pos2.copy());
		} while (true);
		updateSlaves();
		tile().markRender();
		tile().notifyPartChange(this);
	}
	
	public Part7Segment getSegment(BlockCoord crd)
	{
		TileEntity te = world().getTileEntity(crd.x, crd.y, crd.z);
		if(te instanceof TileMultipart)
		{
			TileMultipart tm = (TileMultipart)te;
			TMultiPart multipart = tm.partMap(getSide());
			if(multipart instanceof Part7Segment) 
				return (Part7Segment)multipart;
		}
		return null;
	}
	
	public void updateSlaves()
	{
		if(world().isRemote) return;
		
		int input = 0;
		for(byte[] in : this.input)
		{
			int i2 = 0;
			for(int i = 0; i < in.length; i++)
				i2 |= (in[i] != 0 ? 1 : 0) << i;
			input |= i2;
		}
		
		for(int i = 0; i <= slaves.size(); i++)
		{
			int decimal = (int)Math.floor(input / Math.pow(10, i)) % 10;
			decimal = MathHelper.clamp_int(decimal, 0, 9);
			Part7Segment slave = this;
			if(i > 0)
			{
				BlockCoord bc = slaves.get(i - 1);
				slave = getSegment(bc);
			}
			if(slave != null)
			{
				int odisp = slave.display;
				slave.display = NUMBERS[decimal];
				if(odisp != slave.display)
					slave.getWriteStream(10).writeInt(slave.display);
			}
		}
	}
	
	@Override
	public void updateInput() 
	{
		super.updateInput();
		if(!isSlave) updateSlaves();
	}

	@Override
	public void load(NBTTagCompound tag)
	{
		super.load(tag);
		display = tag.getInteger("display");
		isSlave = tag.getBoolean("isSlave");
		if(isSlave)
			parent = new BlockCoord(tag.getIntArray("parent"));
		else
		{
			this.slaves = Lists.newArrayList();
			NBTTagList slaves = tag.getTagList("slaves", NBT.TAG_INT_ARRAY);
			for(int i = 0; i < slaves.tagCount(); i++)
				this.slaves.add(new BlockCoord(slaves.func_150306_c(i)));
		}
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		super.save(tag);
		tag.setInteger("display", display);
		tag.setBoolean("isSlave", isSlave);
		if(isSlave)
			tag.setIntArray("parent", parent.intArray());
		else
		{
			NBTTagList slaves = new NBTTagList();
			for(BlockCoord slave : this.slaves)
				slaves.appendTag(new NBTTagIntArray(slave.intArray()));
			tag.setTag("slaves", slaves);
		}
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		load(packet.readNBTTagCompound());
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		NBTTagCompound comp = new NBTTagCompound();
		save(comp);
		packet.writeNBTTagCompound(comp);
	}

	@Override
	public void read(byte discr, MCDataInput packet) 
	{
		if(discr == 10)
			display = packet.readInt();
		else super.read(discr, packet);
	}

	@Override
	public ItemStack getItem() 
	{
		return new ItemStack(IntegratedCircuits.item7Segment);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Part7SegmentRenderer getRenderer() 
	{
		return ClientProxy.segmentRenderer;
	}

	@Override
	public boolean canConnectRedstoneImpl(int arg0) 
	{
		return false;
	}

	@Override
	public boolean canConnectBundledImpl(int arg0) 
	{
		return !isSlave;
	}

	@Override
	PartGate newInstance() 
	{
		return new Part7Segment();
	}
}

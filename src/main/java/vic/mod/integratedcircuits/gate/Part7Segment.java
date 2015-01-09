package vic.mod.integratedcircuits.gate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.Constants.NBT;

import org.apache.commons.lang3.StringUtils;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.net.Packet7SegmentOpenGui;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Part7Segment extends PartGate
{
	public int digit = NUMBERS[0];
	public int color;
	public boolean isSlave;
	public boolean hasSlaves;
	public int mode = MODE_SIMPLE;
	
	public BlockCoord parent;
	public ArrayList<BlockCoord> slaves = Lists.newArrayList();
	
	//    0
	//    --
	//  5|6 |1
	//    --
	//  4|3 |2
	//    --    #7
	public static final byte[] NUMBERS = {63, 6, 91, 79, 102, 109, 125, 7, 127, 111, 119, 124, 57, 94, 121, 113}; //0-F
	
	//In reverse order
	public static final byte[] TRUE = {121, 62, 80, 120}; //true
	public static final byte[] FALSE = {121, 109, 56, 119, 113}; //false
	public static final byte[] NAN = {55, 119, 55};
	public static final byte[] INF = {113, 55, 48};
	public static final byte[] INFINITY = {110, 120, 48, 55, 48, 113, 55, 48};
			
	public static final int DOT = 1 << 7;
	public static final int SIGN = 1 << 6;
	public static final int MAX_DIGITS = 16; //TODO Mabye a config option?
	
	public static final int MODE_SIMPLE = 0;
	public static final int MODE_ANALOG = 1;
	public static final int MODE_SHORT_SIGNED = 2;
	public static final int MODE_SHORT_UNSIGNED = 3;
	public static final int MODE_FLOAT = 4;
	public static final int MODE_BINARY_STRING = 5;
	public static final int MODE_MANUAL = 6;
	
	public Part7Segment() 
	{
		super("7segment");
	}

	@Override
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta) 
	{
		super.preparePlacement(player, pos, side, meta);
		color = meta;
	}

	@Override
	public void onActivatedWithScrewdriver(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		if(player.isSneaking())
			CommonProxy.networkWrapper.sendTo(new Packet7SegmentOpenGui(provider), (EntityPlayerMP)player);
		else super.onActivatedWithScrewdriver(player, hit, item);
	}

	@Override
	public void onAdded() 
	{
		super.onAdded();
		if(provider.getWorld().isRemote) return;
		
		isSlave = false;
		int abs = Rotation.rotateSide(getSide(), getRotationAbs(3));
		BlockCoord pos = provider.getPos();
		BlockCoord pos2 = pos.copy();
		Part7Segment seg;
		
		int off = 0;
		do {
			off++;
			pos2.offset(abs);
			seg = getSegment(pos2);
			if(seg == null || seg.getRotation() != getRotation()) break;
			if(seg.isSlave) continue;
			
			parent = pos2;
			isSlave = true;
			
			seg.claimSlaves();	
			seg.updateSlaves();
			
			break;
		} while (off < MAX_DIGITS);
		
		sendChangesToClient();
	}

	@Override
	public void onRemoved() 
	{
		super.onRemoved();
		if(!provider.getWorld().isRemote) updateConnections();
	}
	
	@Override
	public void rotate() 
	{
		updateConnections();
		super.rotate();
		updateInput();
		claimSlaves();
	}
	
	public void updateConnections()
	{
		if(isSlave)
		{
			BlockCoord crd = provider.getPos();
			isSlave = false;
			
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
			BlockCoord crd = provider.getPos().offset(abs);
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
		BlockCoord pos = provider.getPos();
		BlockCoord pos2 = pos.copy();
		Part7Segment seg;
		
		int off = 0;
		do {
			off++;
			pos2.offset(abs);
			seg = getSegment(pos2);
			if(seg == null) break;
			if(seg.isSlave && seg.getRotation() == getRotation()) 
				slaves.add(pos2.copy());
			else break;
		} while (off < MAX_DIGITS);
		
		updateSlaves();
		sendChangesToClient();
	}
	
	public Part7Segment getSegment(BlockCoord crd)
	{
		PartGate gate = GateProvider.getGateAt(provider.getWorld(), crd, getSide());
		if(gate instanceof Part7Segment) return (Part7Segment)gate;
		return null;
	}
	
	private void updateSlaves()
	{
		if(provider.getWorld().isRemote) return;
		
		int input = 0;

		if(mode == MODE_SIMPLE || mode == MODE_ANALOG)
		{
			if(mode == MODE_SIMPLE)
			{
				for(byte[] in : this.input)
					input |= in[0] != 0 ? 1 : 0;
			}
			else
			{
				for(byte[] in : this.input)
					if(in[0] > input) input = in[0];
			}
			
			if(slaves.size() < 4 || mode == MODE_ANALOG)
			{	
				writeDigits(null);
				writeDigit(NUMBERS[input]);
			}
			else 
			{
				byte[] digits = input == 0 ? FALSE : TRUE;
				writeDigits(digits);
			}
		}
		else
		{
			boolean sign = false;
			int length = 16;
			
			for(byte[] in : this.input)
			{
				int i2 = 0;
				for(int i = 0; i < 16; i++)
					i2 |= (in[i] != 0 ? 1 : 0) << i;
				input |= i2;
			}
			
			boolean outOfBounds = false;
			int decimalDot = -1;
			String dispString = "";
			
			if(isSigned()) 
			{
				sign = (input & 32768) != 0;
				input &= 32767;
			}
			
			if(mode == MODE_MANUAL)
			{
				writeDigits(null);
				writeDigit(input & 255);
				return;
			}
			else if(mode == MODE_FLOAT)
			{
				float conv = MiscUtils.toBinary16Float(input);
				if(Float.isNaN(conv) || Float.isInfinite(conv))
				{
					byte[] digits = null;
					if(Float.isNaN(conv) && slaves.size() > 1) digits = NAN;
					else if(Float.isInfinite(conv))
					{
						if(slaves.size() > 7) digits = INFINITY;
						else if(slaves.size() > 2) digits = INF;
					}
					
					if(digits != null) 
					{
						writeDigits(digits);
						if(sign && Float.isInfinite(conv))
						{
							Part7Segment slave = getSegment(slaves.get(slaves.size() - 1));
							if(slave != null) slave.writeDigit(SIGN);
						}
						return;
					}
					else outOfBounds = true;
				}
				else
				{
					int size = slaves.size() - String.valueOf((int)conv).length();
					DecimalFormat df = new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH));
					df.setMaximumFractionDigits(size);
					dispString = df.format(conv);
					decimalDot = dispString.indexOf(".");
					dispString = dispString.replace(".", "");
					if(decimalDot != -1) decimalDot = dispString.length() - decimalDot;	
				}
			}
			else if(mode == MODE_BINARY_STRING)
				dispString = Integer.toBinaryString(input);	
			else dispString = String.valueOf(input);
			
			int size = dispString.length() - 1;
			if(size > slaves.size() - (isSigned() ? 1 : 0)) outOfBounds = true;
			
			dispString = StringUtils.reverse(dispString);
			for(int i = 0; i <= slaves.size(); i++)
			{
				int decimal = i < dispString.length() ? Integer.valueOf(String.valueOf(dispString.charAt(i))) : 0;
				Part7Segment slave = this;
				if(i > 0)
				{
					BlockCoord bc = slaves.get(i - 1);
					slave = getSegment(bc);
				}
				if(slave != null)
				{
					if(i == slaves.size() && isSigned())
						slave.writeDigit(sign ? SIGN : 0);
					else slave.writeDigit(outOfBounds ? SIGN : NUMBERS[decimal] | (decimalDot == i ? DOT : 0));
				}
			}
		}
	}
	
	private void writeDigits(byte[] digits)
	{
		for(int i = 0; i <= slaves.size(); i++)
		{
			Part7Segment slave = this;
			int digit = digits != null && i < digits.length ? digits[i] : 0;	
			if(i > 0)
			{
				BlockCoord bc = slaves.get(i - 1);
				slave = getSegment(bc);
			}
			if(slave != null) slave.writeDigit(digit);
		}
	}	
	
	private void writeDigit(int digit)
	{
		int odisp = this.digit;
		this.digit = digit;
		if(odisp != digit) provider.getWriteStream(10).writeInt(digit);
	}
	
	private boolean isSigned()
	{
		return mode == MODE_SHORT_SIGNED || mode == MODE_FLOAT;
	}
	
	private void sendChangesToClient()
	{
		provider.notifyPartChange();
		hasSlaves = slaves.size() > 0;
		MCDataOutput out = provider.getWriteStream(11);
		out.writeBoolean(isSlave);
		out.writeBoolean(hasSlaves);
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
		digit = tag.getInteger("display");
		isSlave = tag.getBoolean("isSlave");
		color = tag.getInteger("color");
		mode = tag.getInteger("mode");
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
		tag.setInteger("display", digit);
		tag.setBoolean("isSlave", isSlave);
		tag.setInteger("color", color);
		tag.setInteger("mode", mode);
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
		digit = packet.readInt();
		color = packet.readInt();
		isSlave = packet.readBoolean();
		hasSlaves = packet.readBoolean();
		mode = packet.readInt();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeInt(digit);
		packet.writeInt(color);
		packet.writeBoolean(isSlave);
		packet.writeBoolean(slaves.size() > 0);
		packet.writeInt(mode);
	}

	@Override
	public void read(byte discr, MCDataInput packet) 
	{
		if(discr == 10)
			digit = packet.readInt();
		else if(discr == 11)
		{
			isSlave = packet.readBoolean();
			hasSlaves = packet.readBoolean();
			provider.markRender();
		}
		else super.read(discr, packet);
	}

	@Override
	public ItemStack getItem() 
	{
		return new ItemStack(IntegratedCircuits.item7Segment, 1, color);
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
		return !isSlave && mode < 2;
	}

	@Override
	public boolean canConnectBundledImpl(int arg0) 
	{
		return !isSlave && mode > 1;
	}

	@Override
	public PartGate newInstance() 
	{
		return new Part7Segment();
	}
}

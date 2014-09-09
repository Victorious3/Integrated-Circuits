package vic.mod.integratedcircuits;

import java.util.Arrays;
import java.util.Random;

import mrtjp.projectred.integration.BundledGateLogic;
import mrtjp.projectred.integration.BundledGatePart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
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
	@SideOnly(Side.CLIENT)
	public static PartCircuitRenderer renderer = new PartCircuitRenderer();
	
	public short tier;
	public String name;
	public byte[][] output;
	public int[][][] matrix;
	
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
		state = (byte)new Random().nextInt(15);
		tier = 2;
		name = "IC_" + new Random().nextInt(10);
		genOutput();
		genMatrix();
		scheduleTick(0);
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
		tier = tag.getShort("tier");
		name = tag.getString("name");
		genMatrix();
		matrix = MiscUtils.readPCBMatrix(tag);
		genOutput();
	}
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		super.save(tag);
		
		tag.setShort("tier", tier);
		tag.setString("name", name);
		MiscUtils.writePCBMatrix(tag, matrix);
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
		tier = packet.readShort();
		name = packet.readString();
		genMatrix();
		matrix = MiscUtils.readPCBMatrix(packet.readNBTTagCompound());
		genOutput();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet) 
	{
		super.writeDesc(packet);
		
		packet.writeShort(tier);
		packet.writeString(name);
		NBTTagCompound compound = new NBTTagCompound();
		MiscUtils.writePCBMatrix(compound, matrix);
		packet.writeNBTTagCompound(compound);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(Random rand) 
	{
		//Nothing fancy in here...
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
			renderer.prepare(this);
			renderer.renderStatic(pos.translation(), orientation & 0xFF);
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
			renderer.prepareDynamic(this, frame);
			renderer.renderDynamic(this.rotationT().with(pos.translation()));
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
		public void onChange(BundledGatePart gate) 
		{
			
		}

		@Override
		public byte[] getBundledOutput(BundledGatePart gate, int r) 
		{
			return isBundeledAtSide(r) ? output[r] : null;
		}

		@Override
		public void scheduledTick(BundledGatePart gate) 
		{
			super.scheduledTick(gate);
			System.out.println("I ticked");
		}
	}
	
	@Override
	public byte[] getBundledSignal(int r) 
	{
		//Why would I need a mask?
		return getLogic().getBundledOutput(this, toInternal(r));
	}

	private boolean isBundeledAtSide(int s)
	{
		int mask = (int)Math.pow(2, s);
		return (state & mask) > 0;
	}
	
	private void genMatrix()
	{
		int s = tier == 1 ? 18 : tier == 2 ? 34 : 68;
		matrix = new int[s][s][2];
	}
	
	private void genOutput()
	{
		output = new byte[4][];
		for(int i = 0; i < 4; i++)
		{
			if(isBundeledAtSide(i))
			{
				byte[] b = new byte[16];
				Arrays.fill(b, (byte)255);
				output[i] = b;
			}
			else output[i] = new byte[]{15};
		}
	}

	@Override
	public int[][][] getMatrix() 
	{
		return matrix;
	}
	
	@Override
	public void setMatrix(int[][][] matrix) 
	{
		this.matrix = matrix;
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

	@Override
	public void scheduleTick(int x, int y) 
	{
		// TODO Auto-generated method stub
	}
}

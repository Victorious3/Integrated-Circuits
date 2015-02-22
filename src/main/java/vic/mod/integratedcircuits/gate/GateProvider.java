package vic.mod.integratedcircuits.gate;

import mods.immibis.redlogic.wires.RedAlloyTile;
import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetNetworkContainer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.RedstoneInteractions;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.ComputerCraftAPI;

public class GateProvider
{
	private GateProvider() {}
	
	public interface IGateProvider 
	{
		public void markRender();
		
		public MCDataOutput getWriteStream(int disc);
		
		public World getWorld();
		
		public void notifyBlocksAndChanges();
		
		public void notifyPartChange();
		
		public BlockCoord getPos();
		
		public TileEntity getTileEntity();
		
		public void destroy();
		
		public int updateRedstoneInput(int side);
		
		public byte[] updateBundledInput(int side);
		
		public void scheduleTick(int delay);
		
		public PartGate getGate();
		
		public int strongPowerLevel(int side);
		
		public ItemStack getItemStack();
		
		public boolean isMultipart();
	}
	
	private static final int[] vanillaSideMap = {1, 2, 5, 3, 4};
	
	public static int vanillaToSide(int vside)
	{
		return vanillaSideMap[vside + 1];
	}
	
	public static PartGate getGateAt(World world, BlockCoord pos, int side)
	{
		TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
		if(IntegratedCircuits.isFMPLoaded && te instanceof TileMultipart)
		{
			TileMultipart tm = (TileMultipart)te;
			TMultiPart multipart = tm.partMap(side);
			if(multipart instanceof IGateProvider) 
				return ((IGateProvider)multipart).getGate();
		}
		else if(te instanceof TileEntityGate)
		{
			TileEntityGate gate = (TileEntityGate)te;
			if(gate.getGate().getSide() == side) return gate.getGate();
		}
		return null;
	}
	
	public static byte[] calculateBundledInput(IGateProvider provider, int side)
	{
		int r = provider.getGate().getRotationAbs(side);
		int face = provider.getGate().getSide();
		int abs = Rotation.rotateSide(face, r);
		BlockCoord pos = provider.getPos().offset(abs);
		
		byte[] input = null;
		if(IntegratedCircuits.isMFRLoaded && input == null)
		{
			//Ignore MFR tiles, they update separately.
			Block block = provider.getWorld().getBlock(pos.x, pos.y, pos.z);
			if(block instanceof IRedNetNetworkContainer) return provider.getGate().input[side];
		}
		
		input = calculateBundledInputNative(provider, side, pos, abs);
		if(input == null && IntegratedCircuits.isPRLoaded) input = calculateBundledInputProjectRed(provider, side);
		
		if(!provider.isMultipart())
		{
			if(IntegratedCircuits.isRLLoaded && input == null) input = calculateBundledInputRedLogic(provider, side, pos, abs);
			if(input == null) input = calculateBundledInputComputercraft(provider, side, pos, abs);
		}

		if(input == null) input = new byte[16];
		return input;
	}
	
	/** Used to update the input coming from other gates, in case no API for bundled cabling is present **/
	private static byte[] calculateBundledInputNative(IGateProvider provider, int side, BlockCoord pos, int abs)
	{
		PartGate neighbour = getGateAt(provider.getWorld(), pos, provider.getGate().getSide());
		if(neighbour != null) return neighbour.output[side];
		return null;
	}
	
	private static byte[] calculateBundledInputComputercraft(IGateProvider provider, int side, BlockCoord pos, int abs)
	{
		int input = ComputerCraftAPI.getBundledRedstoneOutput(provider.getWorld(), pos.x, pos.y, pos.z, abs ^ 1);
		if(input > 0)
		{
			//digital to analog
			byte[] convInput = new byte[16];
			for(int i = 0; i < 16; i++)
			{
				convInput[i] = (byte)(input & 1);
				input >>= 1;
			}
			return convInput;
		}
		return null;
	}
	
	@Method(modid = "RedLogic")
	private static byte[] calculateBundledInputRedLogic(IGateProvider provider, int side, BlockCoord pos, int abs)
	{
		byte[] power = null;
		TileEntity te = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
		if(te instanceof mods.immibis.redlogic.api.wiring.IBundledEmitter)
		{
			mods.immibis.redlogic.api.wiring.IBundledEmitter emitter = (mods.immibis.redlogic.api.wiring.IBundledEmitter) te;
			power = emitter.getBundledCableStrength(provider.getGate().getSide(), abs ^ 1);
		}	
		return power;
	}
	
	@Method(modid = "ProjRed|Transmission")
	private static byte[] calculateBundledInputProjectRed(IGateProvider provider, int side)
	{
		int r = provider.getGate().getRotationAbs(side);
		int face = provider.getGate().getSide();
		int abs = Rotation.rotateSide(face, r);	
		
		byte[] power = null;

		//Corner signal
		if(provider.isMultipart())
		{
			if(((abs ^ 1) & 6) != ((face ^ 1) & 6))
			{
				BlockCoord pos = provider.getPos().offset(abs).offset(face);
				TileEntity t = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
				if(t != null && t instanceof TileMultipart) 
					power = updateBundledPartSignal(0, ((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, face ^ 1));
				if(power != null) return power;
			}
		}
		
		//Straight signal
		BlockCoord pos = provider.getPos().offset(abs);
		TileEntity t = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
		
		if(t instanceof IBundledEmitter)
			power = updateBundledPartSignal(1, t, abs ^ 1);
		else if(t instanceof TileMultipart)
			power = updateBundledPartSignal(1, ((TileMultipart)t).partMap(face), (r + 2) % 4);
		else power = APIImpl_Transmission.getBundledSignal(provider.getWorld(), pos, abs ^ 1);

		if(power != null) return power;
		
		//Internal signal
		if(provider.isMultipart())
		{
			if((abs & 6) != (face & 6))
			{
				TMultiPart tp = ((TileMultipart)provider.getTileEntity()).partMap(abs);
				power = updateBundledPartSignal(2, tp, Rotation.rotationTo(abs, face));
				if(power != null) return power;
			}
		}
		
		return null;
	}
	
	@Method(modid = "ProjRed|Transmission")
	private static byte[] updateBundledPartSignal(int mode, Object part, int r)
	{
		if(part instanceof IBundledEmitter) return ((IBundledEmitter)part).getBundledSignal(r);
		return null;
	}
	
	public static int calculateRedstoneInput(IGateProvider provider, int side)
	{
		int r = provider.getGate().getRotationAbs(side);
		int face = provider.getGate().getSide();
		int abs = Rotation.rotateSide(face, r);
		if(provider.strongPowerLevel(abs) != 0) return 0;
		int power = 0;
		
		if(provider.isMultipart())
		{
			//ProjectRed
			
			//Corner signal
			if(((abs ^ 1) & 6) != ((face ^ 1) & 6))
			{
				BlockCoord pos = provider.getPos().offset(abs).offset(face);
				TileEntity t = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
				if(t != null && t instanceof TileMultipart) 
					power = updatePartSignal(((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, face ^ 1));
				if(power > 0) return power / 17;
			}
			
			//Straight signal / Vanilla input
			power = RedstoneInteractions.getPowerTo((TMultiPart)provider, abs);
			if(power > 0) return power;
			
			//Internal signal
			TMultiPart tp = ((TileMultipart)provider.getTileEntity()).partMap(abs);
			if((abs & 6) != (face & 6))
			{
				power = updatePartSignal(tp, Rotation.rotationTo(abs, face));
				if(power > 0) return power / 17;
			}	
			if(tp instanceof IRedstonePart)
			{
				IRedstonePart rp = (IRedstonePart)tp;
				power = Math.max(rp.strongPowerLevel(face), rp.weakPowerLevel(face)) << 4;
				if(power > 0) return power;
			}
		}
		else
		{
			BlockCoord pos = provider.getPos().offset(abs);
			//Vanilla input
			power = provider.getWorld().getIndirectPowerLevelTo(pos.x, pos.y, pos.z, abs);
			
			if(power == 0 && IntegratedCircuits.isRLLoaded)
			{
				//Exclude jacketed wire, they don't connect.
				TileEntity te = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
				if(te instanceof RedAlloyTile)
				{
					if(((RedAlloyTile)te).hasJacketedWire()) power = 0;
				}
			}
			if(power == 0 && IntegratedCircuits.isMFRLoaded)
			{
				//Ignore MFR tiles, they update separately.
				Block block = provider.getWorld().getBlock(pos.x, pos.y, pos.z);
				if(block instanceof IRedNetNetworkContainer) power = provider.getGate().input[side][0];
			}
			
			//Compatibility to redstone
			if(power < 15 && provider.getWorld().getBlock(pos.x, pos.y, pos.z) == Blocks.redstone_wire)
				power = Math.max(power, provider.getWorld().getBlockMetadata(pos.x, pos.y, pos.z));
		}
		
		return power;
	}
	
	@Method(modid = "ForgeMultipart")
	private static int updatePartSignal(TMultiPart part, int r)
	{
		if(!IntegratedCircuits.isPRLoaded) return 0;
		if(part instanceof IRedwireEmitter) 
			return ((IRedwireEmitter)part).getRedwireSignal(r);
		return 0;
	}
}

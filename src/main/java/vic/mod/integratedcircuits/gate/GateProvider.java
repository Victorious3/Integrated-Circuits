package vic.mod.integratedcircuits.gate;

import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
		if(!IntegratedCircuits.isPRLoaded) return new byte[16];
		return calculateBundledInputImpl(provider, side);
	}
	
	@Method(modid = "ProjRed|Transmission")
	private static byte[] calculateBundledInputImpl(IGateProvider provider, int side)
	{
		int r = provider.getGate().getRotationAbs(side);
		int face = provider.getGate().getSide();
		int abs = Rotation.rotateSide(face, r);	
		
		byte[] power = null;

		if(provider.getTileEntity() instanceof TileMultipart)
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
		
		BlockCoord pos = provider.getPos().offset(abs);
		TileEntity t = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
		
		if(t instanceof IBundledEmitter)
			power = updateBundledPartSignal(1, t, abs ^ 1);
		else if(t instanceof TileMultipart)
			power = updateBundledPartSignal(1, ((TileMultipart)t).partMap(face), (r + 2) % 4);
		else if(IntegratedCircuits.isPRLoaded)
			power = APIImpl_Transmission.getBundledSignal(provider.getWorld(), pos, abs ^ 1);
		
		if(power != null) return power;
		
		if(provider.getTileEntity() instanceof TileMultipart)
		{
			if((abs & 6) != (face & 6))
			{
				TMultiPart tp = ((TileMultipart)provider.getTileEntity()).partMap(abs);
				power = updateBundledPartSignal(2, tp, Rotation.rotationTo(abs, face));
				if(power != null) return power;
			}
		}
		
		return new byte[16];
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
		
		if(IntegratedCircuits.isFMPLoaded && provider.getTileEntity() instanceof TileMultipart)
		{
			if(((abs ^ 1) & 6) != ((face ^ 1) & 6))
			{
				BlockCoord pos = provider.getPos().offset(abs).offset(face);
				TileEntity t = provider.getWorld().getTileEntity(pos.x, pos.y, pos.z);
				if(t != null && t instanceof TileMultipart) 
					power = updatePartSignal(((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, face ^ 1));
				if(power > 0) return power / 17;
			}
		
			power = RedstoneInteractions.getPowerTo((TMultiPart)provider, abs);
			if(power > 0) return power;
			
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
			power = provider.getWorld().getIndirectPowerLevelTo(pos.x, pos.y, pos.z, side ^ 1);
			pos.offset(abs);
			
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

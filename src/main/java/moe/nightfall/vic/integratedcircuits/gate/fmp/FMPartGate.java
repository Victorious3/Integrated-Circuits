package moe.nightfall.vic.integratedcircuits.gate.fmp;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;

@InterfaceList(value = {
	@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
	@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core"),
	@Interface(iface = "com.bluepowermod.api.wire.redstone.IBundledDeviceWrapper", modid = "bluepower")
})
public class FMPartGate /*extends JCuboidPart implements 
	JNormalOcclusion, TFacePart, IConnectable, IFaceRedstonePart, 
	IBundledEmitter, IBundledDeviceWrapper*/
{

	/*private Gate gate;
	
	//TODO Re-implement
	private BPDevice bpDevice;
	
	@Override
	public String getType() 
	{
		return Constants.MOD_ID + ".socket_fmp";
	}
	
	@Override
	public void load(NBTTagCompound tag)
	{
		gate.load(tag);
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		gate.save(tag);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		gate.readDesc(packet);
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		gate.writeDesc(packet);
	}

	@Override
	public void read(MCDataInput packet) 
	{
		gate.read(packet.readByte(), packet);
	}
	
	@Override
	public MCDataOutput getWriteStream(int disc)
	{
		return getWriteStream().writeByte(disc);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return Gate.box.copy().apply(gate.getRotationTransformation());
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() 
	{
		return Arrays.asList(getBounds());
	}
	
	@Override
	public boolean occlusionTest(TMultiPart npart)
	{
		return NormalOcclusionTest.apply(this, npart);
	}
	
	@Override
	public int getSlotMask() 
	{
		return 1 << gate.getSide();
	}

	@Override
	public int redstoneConductionMap() 
	{
		return 0;
	}
	
	@Override
	public void update() 
	{
		gate.update();
	}

	@Override
	public void scheduledTick() 
	{
		gate.scheduledTick();
	}

	@Override
	public boolean solid(int arg0) 
	{
		return false;
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		return gate.activate(player, hit, item);
	}
	
	@Override
	public void onAdded() 
	{
		gate.onAdded();
	}

	@Override
	public void onRemoved()
	{
		gate.onRemoved();
	}

	@Override
	public void onMoved() 
	{
		gate.onMoved();
	}
	
	@Override
	public Iterable<ItemStack> getDrops() 
	{
		return Arrays.asList(gate.getItemStack());
	}
	
	@Override
	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return gate.pickItem(hit);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0) 
		{
			ClientProxy.socketRendererFMP.prepare(this);
			ClientProxy.socketRendererFMP.renderStatic(new Translation(pos), 0);
			return true;
		}
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) 
	{
		if(pass == 0) 
		{
    		ClientProxy.socketRendererFMP.prepareDynamic(this, frame);
    		ClientProxy.socketRendererFMP.renderDynamic(new Translation(pos));
		}
	}
	
	@Override
	public void onNeighborChanged() 
	{
		gate.onNeighborChanged();
	}

	@Override
	public void onPartChanged(TMultiPart part) 
	{
		if(!world().isRemote) gate.updateInput();
	}
	
	//ProjectRed
	
	@Override
	public boolean canConnectCorner(int arg0) 
	{
		return false;
	}

	@Override
	public boolean connectCorner(IConnectable arg0, int arg1, int arg2) 
	{
		return connectStraight(arg0, arg1, arg2);
	}

	@Override
	public boolean connectInternal(IConnectable arg0, int arg1) 
	{
		return connectStraight(arg0, arg1, 0);
	}

	@Override
	public boolean connectStraight(IConnectable arg0, int arg1, int arg2) 
	{
		int side = gate.getRotationRel(arg1);
		if(arg0 instanceof IRedwireEmitter && gate.canConnectRedstone(side)) return true;
		else if(arg0 instanceof IBundledEmitter && gate.canConnectBundledl(side)) return true;
		return false;
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		int rot = gate.getRotationRel(arg0);
		if(!gate.canConnectBundledl(rot)) return null;
		return gate.getBundledOutput(rot);
	}
	
	//---

	@Override
	public final boolean canConnectRedstone(int arg0) 
	{
		if((arg0 & 6) == (gate.getSide() & 6)) return false;
		return gate.canConnectRedstone(gate.getSideRel(arg0));
	}
	
	@Override
	public int strongPowerLevel(int arg0) 
	{
		if((arg0 & 6) == (gate.getSide() & 6)) return 0;
		int rot = gate.getSideRel(arg0);
		if(!gate.canConnectRedstone(rot)) return 0;
		return gate.getRedstoneOutput(rot);
	}

	@Override
	public int weakPowerLevel(int arg0) 
	{
		return strongPowerLevel(arg0);
	}

	@Override
	public int getFace() 
	{
		return gate.getSide();
	}

	@Override
	public void markRender() 
	{
		tile().markRender();
	}

	@Override
	public World getWorld() 
	{
		return world();
	}
	
	@Override
	public void notifyPartChange() 
	{
		tile().notifyPartChange(this);
	}

	@Override
	public void notifyBlocksAndChanges() 
	{
		tile().markDirty();
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(gate.getSide());
	}

	@Override
	public BlockCoord getPos() 
	{
		return new BlockCoord(x(), y(), z());
	}

	@Override
	public TileEntity getTileEntity() 
	{
		return tile();
	}

	@Override
	public void destroy() 
	{
		tile().remPart(this);
	}
	
	@Override
	public byte[] updateBundledInput(int side)
	{
		return Socket.calculateBundledInput(this, side);
	}
	
	@Override
	public int updateRedstoneInput(int side)
	{
		return Socket.calculateRedstoneInput(this, side);
	}

	@Override
	public Gate getGate() 
	{
		return gate;
	}

	@Override
	@Method(modid = "bluepower")
	public IBundledDevice getBundledDeviceOnSide(ForgeDirection side)
	{
		return bpDevice;
	}*/
}

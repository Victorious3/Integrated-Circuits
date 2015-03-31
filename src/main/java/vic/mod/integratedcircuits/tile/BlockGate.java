package vic.mod.integratedcircuits.tile;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.gate.GateIO;
import vic.mod.integratedcircuits.gate.IGatePeripheralProvider;
import vic.mod.integratedcircuits.gate.ISocket;
import vic.mod.integratedcircuits.gate.ISocket.EnumConnectionType;
import vic.mod.integratedcircuits.gate.Socket;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode", modid = "MineFactoryReloaded")
public class BlockGate extends BlockContainer implements IBundledRedstoneProvider, IRedNetOmniNode, IPeripheralProvider
{
	public BlockGate() 
	{
		super(Material.circuits);
		setBlockName(Constants.MOD_ID + ".gate");
		setHardness(1);
		
		Socket.box.setBlockBounds(this);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) 
	{
		ArrayList<ItemStack> drops = Lists.newArrayList();
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te != null) te.getSocket().addDrops(drops);
		return drops;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te == null) return null;
		return te.getSocket().pickItem(target);
	}	

	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) 
	{
		return true;
	}

	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) 
	{
		return true;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te == null) return;
		ISocket socket = te.getSocket();
		//TODO Move to some sensible function
		Transformation rotation = Rotation.sideOrientation(socket.getSide(), socket.getRotation()).at(Vector3.center);
		Cuboid6 bounds = Socket.box.copy().apply(rotation);
		bounds.setBlockBounds(this);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) 
	{
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getSocket().scheduledTick();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(!te.isDestroyed) te.getSocket().onNeighborChanged();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		return te.getSocket().activate(player, new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(hitX, hitY, hitZ)), player.getHeldItem());
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getSocket().onRemoved();
	}

	@Override
	public boolean renderAsNormalBlock() 
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}

	@Override
	public int getRenderType() 
	{
		return Constants.GATE_RENDER_ID;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return new TileEntityGate();
	}

	@Override
	public boolean canProvidePower() 
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) 
	{
		side = GateIO.vanillaToSide(side);
		
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		if((side & 6) == (socket.getSide() & 6)) return false;
		int rel = socket.getSideRel(side);

		return socket.getConnectionTypeAtSide(rel).isRedstone();
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) 
	{
		return isProvidingStrongPower(world, x, y, z, side);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) 
	{
		side ^= 1;
		
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		if((side & 6) == (socket.getSide() & 6)) return 0;
		int rot = socket.getSideRel(side);
		if(!socket.getConnectionTypeAtSide(side).isRedstone()) return 0;
		
		return socket.getRedstoneOutput(rot);
	}
	
	//Computercraft

	@Override
	public int getBundledRedstoneOutput(World world, int x, int y, int z, int side) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		if((side & 6) == (socket.getSide() & 6)) return -1;
		int rel = socket.getSideRel(side);
		
		//convert analog to digital
		int out = 0;
		for(int i = 0; i < 16; i++)
			out |= (socket.getBundledOutput(side, i) != 0 ? 1 : 0) << i;
		return out;
	}
	
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) 
	{
		ISocket socket = ((TileEntityGate)world.getTileEntity(x, y, z)).getSocket();
		if(socket.getGate() instanceof IGatePeripheralProvider)
		{
			IGatePeripheralProvider provider = (IGatePeripheralProvider)socket.getGate();
			return provider.hasPeripheral(side) ? provider.getPeripheral() : null;
		}
		return null;
	}
	
	//MFR Rednet

	@Override
	@Method(modid = "MineFactoryReloaded")
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection fd) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		int side = fd.ordinal();
		if((side & 6) == (socket.getSide() & 6)) return RedNetConnectionType.None;
		int rel = socket.getSideRel(side);
		
		EnumConnectionType type = socket.getConnectionTypeAtSide(rel);
		if(type.isBundled()) return RedNetConnectionType.PlateAll;
		else if(type.isRedstone()) return RedNetConnectionType.PlateSingle;
		return RedNetConnectionType.None;
	}
	
	@Override
	@Method(modid = "MineFactoryReloaded")
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection fd, int[] inputValues) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		int side = fd.ordinal();
		if((side & 6) == (socket.getSide() & 6)) return;
		int rel = socket.getSideRel(side);
		
		socket.updateInputPre();
		for(int i = 0; i < 16; i++)
			socket.setInput(rel, i, (byte)(inputValues[i] & 0xFF));
		socket.updateInputPost();	
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection fd, int inputValue) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		int side = fd.ordinal();
		if((side & 6) == (socket.getSide() & 6)) return;
		int rel = socket.getSideRel(side);
		
		socket.updateInputPre();
		socket.setInput(rel, 0, (byte)(inputValue & 0xFF));
		socket.updateInputPost();
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection fd) 
	{		
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		int side = fd.ordinal();
		if((side & 6) == (socket.getSide() & 6)) return new int[16];
		int rel = socket.getSideRel(side);
		
		//Convert byte array output to int array, just for you MFR
		int[] out = new int[16];
		byte[] bout = socket.getOutput()[rel];
		for(int i = 0; i < 16; i++)
			out[i] = bout[i] & 255;
		
		return out;
	}

	@Override
	@Method(modid = "MineFactoryReloaded")
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection fd, int subnet) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();
		
		int side = fd.ordinal() ^ 1;
		if((side & 6) == (socket.getSide() & 6)) return 0;
		int rel = socket.getSideRel(side);
		
		return socket.getRedstoneOutput(rel);
	}
}

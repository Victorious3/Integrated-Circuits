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
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.gate.GateProvider;
import vic.mod.integratedcircuits.gate.IGatePeripheralProvider;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.lib.vec.Cuboid6;

import com.google.common.collect.Lists;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;

public class BlockGate extends BlockContainer implements IBundledRedstoneProvider, IRedNetOmniNode, IPeripheralProvider
{
	public BlockGate() 
	{
		super(Material.circuits);
		setBlockName(Constants.MOD_ID + ".gate");
		setHardness(1);
		
		PartGate.box.setBlockBounds(this);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) 
	{
		ArrayList<ItemStack> drops = Lists.newArrayList();
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te != null) drops.add(te.getItemStack());
		return drops;
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te != null)
		{
			if(!world.isRemote && !player.capabilities.isCreativeMode)
				MiscUtils.dropItem(world, te.getItemStack(), x, y, z);
		}
		return world.setBlockToAir(x, y, z);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		return te.getItemStack();
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
		Cuboid6 bounds = PartGate.box.copy().apply(te.getGate().getRotationTransformation());
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
		te.getGate().scheduledTick();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onNeighborChanged();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		return te.getGate().activate(player, new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(hitX, hitY, hitZ)), player.getHeldItem());
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onRemoved();
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
		side = GateProvider.vanillaToSide(side);
		
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();

		if((side & 6) == (gate.getSide() & 6)) return false;
		int rel = gate.getSideRel(side);

		return gate.canConnectRedstoneImpl(rel);
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
		PartGate gate = te.getGate();

		if((side & 6) == (gate.getSide() & 6)) return 0;
		int rot = gate.getSideRel(side);
		if(!gate.canConnectRedstoneImpl(rot)) return 0;
		return gate.getRedstoneOutput(rot);
	}
	
	//Computercraft

	@Override
	public int getBundledRedstoneOutput(World world, int x, int y, int z, int side) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		if((side & 6) == (gate.getSide() & 6)) return -1;
		int rel = gate.getSideRel(side);
		
		//convert analog to digital
		int out = 0;
		for(int i = 0; i < 16; i++)
			out |= (gate.output[side][i] != 0 ? 1 : 0) << i;
		return out;
	}
	
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		if(te.getGate() instanceof IGatePeripheralProvider)
		{
			IGatePeripheralProvider provider = (IGatePeripheralProvider)te.getGate();
			return provider.hasPeripheral(side) ? provider.getPeripheral() : null;
		}
		return null;
	}
	
	//MFR Rednet

	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y, int z, ForgeDirection fd) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		int side = fd.ordinal();
		if((side & 6) == (gate.getSide() & 6)) return RedNetConnectionType.None;
		int rel = gate.getSideRel(side);
		
		if(gate.canConnectBundledImpl(rel)) return RedNetConnectionType.PlateAll;
		else if(gate.canConnectRedstoneImpl(rel)) return RedNetConnectionType.PlateSingle;
		return RedNetConnectionType.None;
	}
	
	@Override
	public void onInputsChanged(World world, int x, int y, int z, ForgeDirection fd, int[] inputValues) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		int side = fd.ordinal();
		if((side & 6) == (gate.getSide() & 6)) return;
		int rel = gate.getSideRel(side);
		
		gate.updateInputPre();
		for(int i = 0; i < 16; i++)
			gate.input[rel][i] = (byte)MathHelper.clamp_int(inputValues[i], 0, 127);
		gate.updateInputPost();		
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z, ForgeDirection fd, int inputValue) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		int side = fd.ordinal();
		if((side & 6) == (gate.getSide() & 6)) return;
		int rel = gate.getSideRel(side);
		
		gate.updateInputPre();
		gate.input[rel][0] = (byte)MathHelper.clamp_int(inputValue, 0, 127);
		gate.updateInputPost();
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z, ForgeDirection fd) 
	{		
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		int side = fd.ordinal();
		if((side & 6) == (gate.getSide() & 6)) return new int[16];
		int rel = gate.getSideRel(side);
		
		//Convert byte array output to int array, just for you MFR
		int[] out = new int[16];
		byte[] bout = gate.output[rel];
		for(int i = 0; i < 16; i++)
			out[i] = bout[i] & 255;
		
		return out;
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z, ForgeDirection fd, int subnet) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		int side = fd.ordinal() ^ 1;
		if((side & 6) == (gate.getSide() & 6)) return 0;
		int rel = gate.getSideRel(side);
		
		return gate.getRedstoneOutput(rel);
	}
}

package vic.mod.integratedcircuits.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.gate.fmp.FMPartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartGate extends Item implements TItemMultiPart
{	
	public ItemPartGate(String name, PartGate gate, ItemGatePair parent, boolean isMultiPart) 
	{
		this.parent = parent;
		this.gate = gate;
		this.isMultiPart = isMultiPart;
		if(isMultiPart) fmpType = new FMPartGate(gate).getType();
		
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(Constants.MOD_ID + "." + name);
		GameRegistry.registerItem(this, Constants.MOD_ID + "_" + name + (isMultiPart ? "_fmp" : ""), Constants.MOD_ID);
		
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
			MinecraftForgeClient.registerItemRenderer(this, gate.getRenderer());
	}
	
	@Override
	@Method(modid = "ForgeMultipart")
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5) 
	{
		FMPartGate part = (FMPartGate)MultiPartRegistry.createPart(fmpType, false);
		part.getGate().preparePlacement(arg1, arg3, arg4, arg0.getItemDamage());
		return part;
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) 
	{
		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 vhit = new Vector3(hitX, hitY, hitZ);
		double d = getHitDepth(vhit, side);

		if(d < 1 && place(stack, player, world, pos, side, vhit))
			return true;
	
		pos.offset(side);
		return place(stack, player, world, pos, side, vhit);
	}
	
	private boolean place(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit)
	{
		BlockCoord pos2 = pos.copy().offset(side ^ 1);
		if(!MiscUtils.canPlaceGateOnSide(world, pos2.x, pos2.y, pos2.z, side))
			return false;
		
		boolean b = false;
		if(isMultiPart) b = placeFMP(stack, player, world, pos, side, vhit);
		else if(world.getBlock(pos.x, pos.y, pos.z).isReplaceable(world, pos.x, pos.y, pos.z))
		{
			PartGate gate = this.gate.newInstance();
			gate.preparePlacement(player, pos, side, stack.getItemDamage());
			world.setBlock(pos.x, pos.y, pos.z, IntegratedCircuits.blockGate);
			TileEntityGate te = (TileEntityGate)world.getTileEntity(pos.x, pos.y, pos.z);
			
			if(te != null)
			{
				te.setGate(gate);
				b = true;
			}
		}
		if(!b) return false;
		
		if(!player.capabilities.isCreativeMode)
			stack.stackSize--;
		
		return true;
	}

	@Method(modid = "ForgeMultipart")
	private boolean placeFMP(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit) 
	{
		TMultiPart part = newPart(stack, player, world, pos, side, vhit);
		if(!TileMultipart.canPlacePart(world, pos, part)) return false;

		if(!world.isRemote)
			TileMultipart.addPart(world, pos, part);
		return true;
	}
	
	@Override
	public double getHitDepth(Vector3 vhit, int side)
	{
		return vhit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) 
	{
		
	}
}

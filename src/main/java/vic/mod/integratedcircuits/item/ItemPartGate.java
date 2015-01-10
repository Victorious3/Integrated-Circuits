package vic.mod.integratedcircuits.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.gate.fmp.FMPartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.tile.BlockGate;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** https://github.com/Chicken-Bones/ForgeMultipart/blob/7bb9ca045df4b6a96bef249fbe81d8ce707de028/src/codechicken/multipart/ItemMultiPart.scala **/
@Interface(iface = "codechicken.multipart.TItemMultiPart", modid = "ForgeMultipart")
public class ItemPartGate extends Item implements TItemMultiPart
{
	private String fmpType;
	private boolean isMultiPart;
	private Block blockType;
	
	public ItemPartGate(String name, PartGate part, boolean isMultiPart) 
	{
		this.isMultiPart = isMultiPart;
		if(isMultiPart) fmpType = new FMPartGate(part).getType();
		
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + "." + name);
		GameRegistry.registerItem(this, IntegratedCircuits.modID + "_" + name + (isMultiPart ? "_fmp" : ""), IntegratedCircuits.modID);
		
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
			MinecraftForgeClient.registerItemRenderer(this, part.getRenderer());
		if(!isMultiPart) 
			GameRegistry.registerBlock(blockType = new BlockGate(part), IntegratedCircuits.modID + "." + name);
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
			world.setBlock(pos.x, pos.y, pos.z, blockType);
			TileEntityGate te = (TileEntityGate)world.getTileEntity(pos.x, pos.y, pos.z);
			te.getGate().preparePlacement(player, pos, side, stack.getItemDamage());
			te.getGate().onAdded();
			b = true;
		}
		if(!b) return false;
		
		if(!player.capabilities.isCreativeMode)
			stack.stackSize -= 1;
		
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
	
	public boolean isMultipartItem()
	{
		return isMultiPart;
	}
	
	public Block getBlockType()
	{
		return blockType;
	}
}

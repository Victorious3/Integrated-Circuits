package vic.mod.integratedcircuits.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.gate.fmp.FMPartGate;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGatePart extends JItemMultiPart
{
	private FMPartGate part;
	
	public ItemGatePart(String name, PartGate part) 
	{
		this.part = new FMPartGate(part);
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(IntegratedCircuits.modID + "." + name);
		setTextureName(IntegratedCircuits.modID + ":" + name);
		GameRegistry.registerItem(this, IntegratedCircuits.modID + "_" + name, IntegratedCircuits.modID);
		
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
			MinecraftForgeClient.registerItemRenderer(this, part.getRenderer());
	}
	
	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5) 
	{
		BlockCoord bc = arg3.copy().offset(arg4 ^ 1);
		if(!MiscUtils.canPlaceGateOnSide(arg2, bc.x, bc.y, bc.z, arg4)) return null;
		FMPartGate part = (FMPartGate)MultiPartRegistry.createPart(this.part.getType(), false);
		part.preparePlacement(arg1, arg3, arg4, arg0.getItemDamage());
		return part;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) 
	{
		
	}
}

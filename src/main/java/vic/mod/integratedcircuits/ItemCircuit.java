package vic.mod.integratedcircuits;

import mrtjp.projectred.core.TItemGlassSound;
import mrtjp.projectred.core.libmc.WireLib;
import mrtjp.projectred.integration.BundledGatePart;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCircuit extends JItemMultiPart implements TItemGlassSound
{
	public ItemCircuit()
	{
		setUnlocalizedName(IntegratedCircuits.modID + ".circuit");
		setTextureName(IntegratedCircuits.modID + ":ic");
		setMaxStackSize(1);
	}

	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5) 
	{
		if(WireLib.canPlaceWireOnSide(arg2, arg3.x, arg3.y, arg3.z, ForgeDirection.getOrientation(arg4), false)) return null;
		BundledGatePart part = (BundledGatePart)MultiPartRegistry.createPart(IntegratedCircuits.partCircuit, false);
		part.preparePlacement(arg1, arg3, arg4, arg0.getItemDamage());
		return part;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getTagCompound() == null) I18n.format(getUnlocalizedName() + ".name", stack.getTagCompound().getString("INVALID!"));
		return I18n.format(getUnlocalizedName() + ".name", stack.getTagCompound().getString("name"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) 
	{
		super.registerIcons(ir);
		ClientProxy.renderer = new PartCircuitRenderer();
		ClientProxy.renderer.registerIcons(ir);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() 
	{
		return 0;
	}
}

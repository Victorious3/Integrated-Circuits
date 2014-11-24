package vic.mod.integratedcircuits.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.part.GatePart;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCircuit extends JItemMultiPart
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
		BlockCoord bc = arg3.copy().offset(arg4 ^ 1);
		if(!MiscUtils.canPlaceGateOnSide(arg2, bc.x, bc.y, bc.z, arg4)) return null;
		GatePart part = (GatePart)MultiPartRegistry.createPart(IntegratedCircuits.partCircuit, false);
		part.preparePlacement(arg1, arg3, arg4, arg0.getItemDamage());
		return part;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getTagCompound() == null) I18n.format(getUnlocalizedName() + ".name", stack.getTagCompound().getString("INVALID!"));
		return I18n.format(getUnlocalizedName() + ".name", stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) 
	{
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

package vic.mod.integratedcircuits.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.fmp.FMPartGate;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemSocketFMP extends JItemMultiPart
{
	@Override
	public TMultiPart newPart(ItemStack stack, EntityPlayer player, World world, BlockCoord crd, int arg4, Vector3 arg5)
	{
		FMPartGate part = (FMPartGate)MultiPartRegistry.createPart(fmpType, false);
		part.getGate().preparePlacement(player, crd, arg4, stack.getItemDamage());
		return part;
	}

	@Override
	public void registerIcons(IIconRegister ir) {}
}

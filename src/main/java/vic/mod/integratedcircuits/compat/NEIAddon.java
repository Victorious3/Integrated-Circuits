package vic.mod.integratedcircuits.compat;

import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.nei.api.API;

public class NEIAddon 
{
	public void initialize()
	{
		if(MiscUtils.isServer()) return;
		
		API.hideItem(new ItemStack(IntegratedCircuits.itemCircuit));
	}
}

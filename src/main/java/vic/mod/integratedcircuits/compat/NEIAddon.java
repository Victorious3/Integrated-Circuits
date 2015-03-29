package vic.mod.integratedcircuits.compat;

import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.IntegratedCircuits;
import codechicken.nei.api.API;

public class NEIAddon 
{	
	public void initialize()
	{	
		API.hideItem(new ItemStack(IntegratedCircuits.itemSocket));
		if(IntegratedCircuits.isFMPLoaded)
			API.hideItem(new ItemStack(IntegratedCircuits.itemSocketFMP));
		
		API.hideItem(new ItemStack(IntegratedCircuits.blockGate));
	}
}

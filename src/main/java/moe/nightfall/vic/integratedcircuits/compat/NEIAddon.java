package moe.nightfall.vic.integratedcircuits.compat;

import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.item.ItemStack;
import codechicken.nei.api.API;

public class NEIAddon {
	public void initialize() {
		API.hideItem(new ItemStack(IntegratedCircuits.itemSocket));
		if (IntegratedCircuits.isFMPLoaded)
			API.hideItem(new ItemStack(IntegratedCircuits.itemSocketFMP));

		API.hideItem(new ItemStack(IntegratedCircuits.blockGate));
	}
}

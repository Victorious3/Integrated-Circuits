package moe.nightfall.vic.integratedcircuits.compat;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.item.ItemStack;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.API;

public class NEIAddon {
	public void initialize() {
		API.hideItem(new ItemStack(Content.itemSocket));
	}

	public static void hideGUI(boolean hide) {
		if (IntegratedCircuits.isNEILoaded) {
			NEIClientConfig.setInternalEnabled(!hide);
		}
	}
}

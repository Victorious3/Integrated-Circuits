package vic.mod.integratedcircuits.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import vic.mod.integratedcircuits.gate.GateRegistry.ItemGatePair;
import vic.mod.integratedcircuits.gate.PartGate;

public class ItemCircuit extends ItemPartGate
{
	public ItemCircuit(String name, PartGate part, ItemGatePair parent, boolean isMultiPart)
	{
		super(name, part, parent, isMultiPart);
		setMaxStackSize(1);
		setCreativeTab(null);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getTagCompound() == null) return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", "INVALID!");
		String name = stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name");
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", name);
	}
}

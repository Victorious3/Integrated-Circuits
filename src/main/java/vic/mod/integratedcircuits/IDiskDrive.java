package vic.mod.integratedcircuits;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public interface IDiskDrive 
{
	public AxisAlignedBB getBoundingBox();
	
	public ItemStack getDisk();
	
	public void setDisk(ItemStack stack);
}

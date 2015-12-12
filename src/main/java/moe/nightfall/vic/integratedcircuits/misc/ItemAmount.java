package moe.nightfall.vic.integratedcircuits.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameData;

public class ItemAmount {
	public double amount;
	public int damageValue;
	public Item item;

	public ItemAmount(Item item, double amount) {
		this.item = item;
		this.amount = amount;
	}

	public ItemAmount(Item item, double amount, int damageValue) {
		this(item, amount);
		this.damageValue = damageValue;
	}

	public static ItemAmount readFromNBT(NBTTagCompound compound) {
		Item item = GameData.getItemRegistry().getRaw(compound.getString("id"));
		int damage = compound.getInteger("damage");
		double amount = compound.getDouble("amount");
		return new ItemAmount(item, amount, damage);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("id", GameData.getItemRegistry().getNameForObject(item));
		compound.setInteger("damage", damageValue);
		compound.setDouble("amount", amount);
		return compound;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + damageValue;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ItemAmount other = (ItemAmount) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
			return false;
		if (damageValue != other.damageValue)
			return false;
		if (item == null && other.item != null)
			return false;
		else if (item != other.item)
			return false;
		return true;
	}

	public boolean hasEqualItem(ItemAmount other) {
		return item == other.item && damageValue == other.damageValue;
	}

	public ItemStack convertToItemStack() {
		return new ItemStack(item, (int) Math.abs(amount), damageValue);
	}

	public ItemStack convertToItemStack(int amount) {
		return new ItemStack(item, amount, damageValue);
	}
}

package moe.nightfall.vic.integratedcircuits.misc;

import java.util.Arrays;

import moe.nightfall.vic.integratedcircuits.net.PacketChangeSetting;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public interface IOptionsProvider
{
	public OptionSet getOptionSet();
	
	public void onSettingChanged(int setting);
	
	public static class OptionSet <T extends TileEntity & IOptionsProvider>
	{
		private int[] options = new int[0];
		private T parent;
		
		public OptionSet(T parent)
		{
			this.parent = parent;
		}
		
		public void changeSettingPayload(int setting, int par)
		{
			resize(setting);
			options[setting] = par;
		}
		
		public void changeSetting(int setting, int par)
		{
			if(parent.getWorldObj().isRemote)
				CommonProxy.networkWrapper.sendToServer(new PacketChangeSetting(parent.xCoord, parent.yCoord, parent.zCoord, setting, par));
			else changeSettingPayload(setting, par);
		}
		
		public void changeSetting(int setting, boolean par)
		{
			changeSetting(setting, par ? 1 : 0);
		}
		
		private void resize(int setting)
		{
			if(options == null) options = new int[setting + 1];
			else if(options != null && setting >= options.length) 
				options = Arrays.copyOf(options, setting + 1);
		}
		
		public int getInt(int setting)
		{
			resize(setting);
			return options[setting];
		}
		
		public boolean getBoolean(int setting)
		{
			resize(setting);
			return options[setting] != 0;
		}
		
		public NBTTagCompound writeToNBT(NBTTagCompound comp)
		{
			comp.setIntArray("options", options);
			return comp;
		}
		
		public static <T extends TileEntity & IOptionsProvider> OptionSet<T> readFromNBT(NBTTagCompound comp, T parent)
		{
			int[] array = comp.getIntArray("options");
			OptionSet<T> set = new OptionSet<T>(parent);
			set.options = array;
			return set;
		}
	}
}



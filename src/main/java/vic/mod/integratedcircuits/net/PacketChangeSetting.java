package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.misc.IOptionsProvider;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketChangeSetting <T extends TileEntity & IOptionsProvider> extends PacketTileEntity
{
	private int setting, param;
	
	public PacketChangeSetting() {}
	
	public PacketChangeSetting(int xCoord, int yCoord, int zCoord, int setting, int param)
	{
		super(xCoord, yCoord, zCoord);
		this.setting = setting;
		this.param = param;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException 
	{
		super.read(buffer);
		this.setting = buffer.readInt();
		this.param = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException 
	{
		super.write(buffer);
		buffer.writeInt(setting);
		buffer.writeInt(param);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		T te = (T) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		if(side == Side.SERVER)
			IntegratedCircuits.networkWrapper.sendToAllAround(this, 
				new TargetPoint(te.getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 8));
		te.getOptionSet().changeSettingPayload(setting, param);
		te.onSettingChanged(setting);
	}
}

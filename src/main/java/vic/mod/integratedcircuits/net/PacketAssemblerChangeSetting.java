package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.client.gui.GuiAssembler;
import vic.mod.integratedcircuits.client.gui.GuiPCBLayout;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerChangeSetting extends PacketTileEntity<PacketAssemblerChangeSetting>
{
	private int setting, param;
	
	public PacketAssemblerChangeSetting() {}
	
	public PacketAssemblerChangeSetting(int xCoord, int yCoord, int zCoord, int setting, int param)
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
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		if(side == Side.SERVER)
			IntegratedCircuits.networkWrapper.sendToAllAround(this, 
				new TargetPoint(te.getWorldObj().provider.dimensionId, xCoord, yCoord, zCoord, 8));
		te.changeSettingPayload(setting, param);
		if(side == Side.CLIENT && Minecraft.getMinecraft().currentScreen instanceof GuiPCBLayout)
			((GuiAssembler)Minecraft.getMinecraft().currentScreen).refreshUI();	
	}
}

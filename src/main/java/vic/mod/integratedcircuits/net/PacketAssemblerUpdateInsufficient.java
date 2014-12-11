package vic.mod.integratedcircuits.net;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import vic.mod.integratedcircuits.misc.ItemAmount;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;

public class PacketAssemblerUpdateInsufficient extends PacketTileEntity<PacketAssemblerUpdateInsufficient>
{
	private Item insufficient;
	
	public PacketAssemblerUpdateInsufficient() {}
	
	public PacketAssemblerUpdateInsufficient(int xCoord, int yCoord, int zCoord, ItemAmount insufficient)
	{
		super(xCoord, yCoord, zCoord);
		this.insufficient = insufficient.item;
	}
	
	@Override
	public void read(PacketBuffer buffer) throws IOException
	{
		super.read(buffer);
		insufficient = GameData.getItemRegistry().getObject(ByteBufUtils.readUTF8String(buffer));
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException
	{
		super.write(buffer);
		ByteBufUtils.writeUTF8String(buffer, GameData.getItemRegistry().getNameForObject(insufficient));
	}

	@Override
	public void process(EntityPlayer player, Side side) 
	{
		TileEntityAssembler te = (TileEntityAssembler)player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if(te == null) return;
		te.craftingSupply.changeInsufficient(new ItemAmount(insufficient, 1));
	}
}

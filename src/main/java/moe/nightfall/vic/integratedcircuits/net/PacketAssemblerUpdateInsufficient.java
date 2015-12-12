package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class PacketAssemblerUpdateInsufficient extends PacketTileEntity<PacketAssemblerUpdateInsufficient> {
	private ItemAmount insufficient;

	public PacketAssemblerUpdateInsufficient() {
	}

	public PacketAssemblerUpdateInsufficient(int xCoord, int yCoord, int zCoord, ItemAmount insufficient) {
		super(xCoord, yCoord, zCoord);
		this.insufficient = insufficient;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		insufficient = ItemAmount.readFromNBT(buffer.readNBTTagCompoundFromBuffer());
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeNBTTagCompoundToBuffer(insufficient.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityAssembler te = (TileEntityAssembler) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te == null)
			return;
		te.craftingSupply.changeInsufficient(insufficient);
	}
}

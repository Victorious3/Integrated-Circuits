package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.cp.CircuitProperties.Comment;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

public class PacketPCBComment extends PacketTileEntity<PacketPCBComment> {

	private Comment comment;

	public PacketPCBComment() {
	}

	public PacketPCBComment(int xCoord, int yCoord, int zCoord, Comment comment) {
		super(xCoord, yCoord, zCoord);
		this.comment = comment;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		comment = Comment.readFromNBT(buffer.readNBTTagCompoundFromBuffer());
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeNBTTagCompoundToBuffer(comment.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);

		if (te != null) {
			if (side.isServer()) {
				CommonProxy.networkWrapper.sendToAllAround(this, new TargetPoint(te.getWorldObj().provider.dimensionId,
						xCoord, yCoord, zCoord, 8));
			}
			te.getCircuitData().getProperties().addComment(comment);
		}
	}

}

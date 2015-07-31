package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangePart extends PacketTileEntity<PacketPCBChangePart> {
	private List<Integer> data;
	private int button = -1;
	private boolean flag;

	public PacketPCBChangePart() {
	}

	public PacketPCBChangePart(int x, int y, int button, boolean ctrl, int tx, int ty, int tz) {
		this(ctrl, tx, ty, tz);
		data = Arrays.asList(x, y);
		this.button = button;
	}

	/**
	 * The flag indicates weather a new snapshot should be taken before
	 * performing the action.
	 **/
	public PacketPCBChangePart(boolean flag, int tx, int ty, int tz) {
		super(tx, ty, tz);
		this.data = new ArrayList<Integer>();
		this.flag = flag;
	}

	public PacketPCBChangePart add(Vec2 pos, int id, int meta) {
		data.add(pos.x);
		data.add(pos.y);
		data.add(id);
		data.add(meta);
		return this;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		button = buffer.readInt();
		flag = buffer.readBoolean();
		int size = buffer.readInt();
		data = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
			data.add(buffer.readInt());
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(button);
		buffer.writeBoolean(flag);
		buffer.writeInt(data.size());
		for (int i : data)
			buffer.writeInt(i);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te != null) {
			CircuitData cdata = te.getCircuitData();

			if (button == -1 && flag)
				te.cache.create(player.getGameProfile().getId());

			for (int i = 0; i < data.size(); i += 4) {
				Vec2 pos = new Vec2(data.get(i), data.get(i + 1));
				if (button != -1)
					cdata.getPart(pos).onClick(pos, te, button, flag);
				else {
					int oid = cdata.getID(pos);
					int ometa = cdata.getMeta(pos);

					if (data.get(i + 2) != oid)
						cdata.getPart(pos).onRemoved(pos, te);

					cdata.setID(pos, data.get(i + 2));
					cdata.setMeta(pos, data.get(i + 3));

					if (data.get(i + 2) != oid)
						cdata.getPart(pos).onPlaced(pos, te);
					else if (data.get(i + 3) != ometa)
						cdata.getPart(pos).onChanged(pos, te, ometa);

					cdata.markForUpdate(pos);
				}
			}
			// Wires must update immediately, even if circuit is not ticked
			cdata.propagateSignals();

			if (button == -1 && flag)
				te.cache.capture(player.getGameProfile().getId());
		}
	}
}

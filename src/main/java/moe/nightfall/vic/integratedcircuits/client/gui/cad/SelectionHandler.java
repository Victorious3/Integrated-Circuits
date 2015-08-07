package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBChangePart;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public class SelectionHandler extends CADHandler {

	private Vec2 selectionStart = Vec2.zero;
	private Vec2 selectionEnd = Vec2.zero;
	private boolean mouseDown = false;

	// TODO Looks about as ugly as it is.
	// TODO Perhaps use the clipboard?
	private int[][][] data = new int[2][0][0];

	private boolean hasSelection() {
		return selectionStart != selectionEnd;
	}

	@Override
	public void onActionPerformed(GuiCAD parent, GuiButton button) {
		if (!hasSelection())
			return;
		switch (button.id) {
			case 94:
				cut(parent);
				break;
			case 95:
				copy(parent);
				break;
			case 96:
				paste(parent);
				break;
			case 97:
				fill(parent);
				break;
		}
	}

	@Override
	public boolean onKeyTyped(GuiCAD parent, int keycode, char ch) {

		switch (ch) {
			case 3:
				copy(parent);
				break; // CTRL + C
			case 24:
				cut(parent);
				break; // CTRL + X
			case 22:
				paste(parent);
				break; // CTRL + V
			case 6:
				fill(parent);
				break; // CTRL + F
			default:
				return false;
		}
		return true;
	}

	private void copy(GuiCAD parent) {
		Vec2 slStart = selectionStart();
		Vec2 slEnd = selectionEnd();
		CircuitData cdata = parent.getCircuitData();

		data = new int[2][slEnd.x - slStart.x][slEnd.y - slStart.y];
		for (int x = slStart.x; x < slEnd.x; x++) {
			for (int y = slStart.y; y < slEnd.y; y++) {
				Vec2 pos = new Vec2(x, y);
				data[0][x - slStart.x][y - slStart.y] = cdata.getID(pos);
				data[1][x - slStart.x][y - slStart.y] = cdata.getMeta(pos);
			}
		}

	}

	private void cut(GuiCAD parent) {
		copy(parent);
		fill(parent, 0, 0);
	}

	private void paste(GuiCAD parent) {
		fill(parent, 0, 0);
		Vec2 slStart = selectionStart();
		CircuitData cdata = parent.getCircuitData();

		PacketPCBChangePart packet = new PacketPCBChangePart(true, parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord);
		for (int x = 0; x < data[0].length; x++) {
			for (int y = 0; y < data[0][x].length; y++) {
				Vec2 pos = new Vec2(x + slStart.x, y + slStart.y);
				packet.add(pos, data[0][x][y], data[1][x][y]);
			}
		}
		CommonProxy.networkWrapper.sendToServer(packet);
	}

	private void fill(GuiCAD parent) {
		if (parent.placeHandler.selectedPart == null)
			return;
		CircuitRenderWrapper crw = parent.placeHandler.selectedPart;
		fill(parent, CircuitPart.getId(crw.getPart()), crw.getState());
	}

	private void fill(GuiCAD parent, int id, int meta) {
		Vec2 slStart = selectionStart();
		Vec2 slEnd = selectionEnd();
		CircuitData cdata = parent.getCircuitData();

		PacketPCBChangePart packet = new PacketPCBChangePart(true, parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord);
		for (int x = slStart.x; x < slEnd.x; x++) {
			for (int y = slStart.y; y < slEnd.y; y++) {
				Vec2 pos = new Vec2(x, y);
				packet.add(pos, id, meta);
			}
		}
		CommonProxy.networkWrapper.sendToServer(packet);
	}

	private Vec2 selectionStart() {
		return new Vec2(Math.min(selectionStart.x, selectionEnd.x), Math.min(selectionStart.y, selectionEnd.y));
	}

	private Vec2 selectionEnd() {
		return new Vec2(Math.max(selectionStart.x + 1, selectionEnd.x + 1), Math.max(selectionStart.y + 1, selectionEnd.y + 1));
	}

	@Override
	public void render(GuiCAD parent, int mx, int my) {
		if (!isActive())
			return;
		if (mouseDown) {
			int gridX = (int) Math.floor(parent.boardAbs2RelX(mx));
			int gridY = (int) Math.floor(parent.boardAbs2RelY(my));

			int w = parent.getCircuitData().getSize() - 2;
			if (gridX < 1)
				gridX = 1;
			else if (gridX > w)
				gridX = w;
			if (gridY < 1)
				gridY = 1;
			else if (gridY > w)
				gridY = w;

			selectionEnd = new Vec2(gridX, gridY);
		}
		
		Vec2 slStart = selectionStart();
		Vec2 slEnd = selectionEnd();
		if (hasSelection()) {
			Gui.drawRect(slStart.x, slStart.y, slEnd.x, slEnd.y, 0x550000FF);
			RenderUtils.drawBorder(slStart.x, slStart.y, slEnd.x - slStart.x, slEnd.y - slStart.y);
		}
	}

	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int bucutton) {
		mouseDown = false;
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {

		if (parent.isShiftKeyDown())
			return;

		int gridX = (int) Math.floor(parent.boardAbs2RelX(mx));
		int gridY = (int) Math.floor(parent.boardAbs2RelY(my));

		selectionStart = selectionEnd = Vec2.zero;
		int w = parent.getCircuitData().getSize() - 1;
		if (gridX > 0 && gridX < w && gridY > 0 && gridY < w) {
			selectionStart = new Vec2(gridX, gridY);
			mouseDown = true;
		}
	}

	@Override
	public void remove(GuiCAD parent) {
		super.remove(parent);
		selectionStart = selectionEnd = Vec2.zero;
	}
}

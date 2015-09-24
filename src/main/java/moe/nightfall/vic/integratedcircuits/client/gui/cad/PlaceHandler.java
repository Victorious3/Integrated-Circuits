package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.cp.part.PartWire;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBCache;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBChangePart;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class PlaceHandler extends CADHandler {

	public CircuitRenderWrapper selectedPart;
	public int currentRotation = 0;
	private boolean mouseDown = false;

	@Override
	public void renderCADCursor(GuiCAD parent, double mouseX, double mouseY, int gridX, int gridY, CircuitData cdata) {
		if (!parent.drag) {

			if (selectedPart.getPart() instanceof PartNull) {
				GL11.glColor3f(0F, 0.4F, 0F);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				Tessellator.instance.startDrawingQuads();
				CircuitPartRenderer.addQuad(gridX, gridY, 0, 0, 1, 1);
				Tessellator.instance.draw();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			} else {
				if (mouseDown) {
					gridX = parent.startX;
					gridY = parent.startY;

					// TODO Render grayed out
				}
			}

			final int PART_SIZE = CircuitPartRenderer.PART_SIZE;
			GL11.glPushMatrix();
			GL11.glScaled(1F / PART_SIZE, 1F / PART_SIZE, 1);
			CircuitPartRenderer.renderPart(selectedPart, gridX * PART_SIZE, gridY * PART_SIZE);
			GL11.glPopMatrix();
			GL11.glColor3f(1, 1, 1);
		} else if (selectedPart.getPart() instanceof PartWire) {
			PartWire wire = (PartWire) selectedPart.getPart();
			switch (wire.getColor(selectedPart.getPos(), selectedPart)) {
				case 1:
					GL11.glColor3f(0.4F, 0F, 0F);
					break;
				case 2:
					GL11.glColor3f(0.4F, 0.2F, 0F);
					break;
				default:
					GL11.glColor3f(0F, 0.4F, 0F);
					break;
			}
			renderDraggedWire(parent);
			GL11.glColor3f(1, 1, 1);
		}
	}

	@Override
	public boolean onMouseWheel(int amount) {
		if (mouseDown && selectedPart.getPart() instanceof PartCPGate) {
			currentRotation++;
			if (currentRotation > 3)
				currentRotation = 0;
			((PartCPGate) selectedPart.getPart()).setRotation(selectedPart.getPos(), selectedPart, currentRotation);
			return true;
		}
		return false;
	}

	private void renderDraggedWire(GuiCAD parent) {
		int x = parent.startX;
		int y = parent.startY;

		Tessellator.instance.startDrawingQuads();
		CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
		if (parent.endY > parent.startY)
			CircuitPartRenderer.addQuad(x, y, 4, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (parent.endY < parent.startY)
			CircuitPartRenderer.addQuad(x, y, 2, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (parent.endX > parent.startX)
			CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
		else if (parent.endX < parent.startX)
			CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);

		while (x != parent.endX || y != parent.endY) {
			if (y < parent.endY)
				y++;
			else if (y > parent.endY)
				y--;
			else if (x < parent.endX)
				x++;
			else if (x > parent.endX)
				x--;

			if (y != parent.endY)
				CircuitPartRenderer.addQuad(x, y, 6, 0, 1, 1, 1, 1, 16, 16, 0);
			else if (y == parent.endY && x == parent.startX) {
				CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
				if (parent.endY > parent.startY)
					CircuitPartRenderer.addQuad(x, y, 2, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (parent.endY < parent.startY)
					CircuitPartRenderer.addQuad(x, y, 4, 0, 1, 1, 1, 1, 16, 16, 0);
				if (parent.endX > parent.startX)
					CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (parent.endX < parent.startX)
					CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);
			} else if (x != parent.endX)
				CircuitPartRenderer.addQuad(x, y, 5, 0, 1, 1, 1, 1, 16, 16, 0);
			else if (x == parent.endX) {
				CircuitPartRenderer.addQuad(x, y, 0, 0, 1, 1, 1, 1, 16, 16, 0);
				if (parent.endX > parent.startX)
					CircuitPartRenderer.addQuad(x, y, 1, 0, 1, 1, 1, 1, 16, 16, 0);
				else if (parent.endX < parent.startX)
					CircuitPartRenderer.addQuad(x, y, 3, 0, 1, 1, 1, 1, 16, 16, 0);
			}
		}
		Tessellator.instance.draw();
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {

		mouseDown = true;

		int gridX = (int) parent.boardAbs2RelX(mx);
		int gridY = (int) parent.boardAbs2RelY(my);
		int w = parent.getBoardSize();

		if (gridX > 0 && gridY > 0 && gridX < w - 1 && gridY < w - 1 && !GuiScreen.isShiftKeyDown()) {
			parent.startX = gridX;
			parent.startY = gridY;
			if (selectedPart.getPart() instanceof PartWire) {
				parent.drag = true;
			}
		}
	}


	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int button) {
		mouseDown = false;

		if (selectedPart.getPart() instanceof PartNull) {
			// Send cache update for erasing
			CommonProxy.networkWrapper.sendToServer(new PacketPCBCache(PacketPCBCache.SNAPSHOT, parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord));
		}

		if (parent.drag) {
			if (selectedPart.getPart() instanceof PartWire) {
				int id = CircuitPart.getId(selectedPart.getPart());
				int state = selectedPart.getState();

				PacketPCBChangePart packet = new PacketPCBChangePart(true, parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord);
				packet.add(new Vec2(parent.startX, parent.startY), id, state);
				while (parent.startX != parent.endX || parent.startY != parent.endY) {
					if (parent.startY < parent.endY)
						parent.startY++;
					else if (parent.startY > parent.endY)
						parent.startY--;
					else if (parent.startX < parent.endX)
						parent.startX++;
					else if (parent.startX > parent.endX)
						parent.startX--;
					packet.add(new Vec2(parent.startX, parent.startY), id, state);
				}
				CommonProxy.networkWrapper.sendToServer(packet);
			}
		} else {
			int gridX = (int) parent.boardAbs2RelX(mx);
			int gridY = (int) parent.boardAbs2RelY(my);

			int w = parent.getCircuitData().getSize();

			if (parent.startX == gridX && parent.startY == gridY) {
				if (parent.startX > 0 && parent.startY > 0 && parent.startX < w - 1 && parent.startY < w - 1 && !GuiScreen.isShiftKeyDown()) {
					int newID = CircuitPart.getId(selectedPart.getPart());

					Vec2 pos = new Vec2(parent.startX, parent.startY);
					if (newID != parent.getCircuitData().getID(pos)) {
						CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(!(selectedPart.getPart() instanceof PartNull), parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord).add(pos, newID, selectedPart.getState()));
					}
				}
			}
		}
	}

	@Override
	public void onMouseDragged(GuiCAD parent, int mx, int my) {
		if (selectedPart.getPart() instanceof PartNull) {
			int boardX = (int) parent.boardAbs2RelX(mx);
			int boardY = (int) parent.boardAbs2RelY(my);
			int w = parent.getBoardSize();
			boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

			if (boardX > 0 && boardY > 0 && boardX < w - 1 && boardY < w - 1 && !shiftDown) {
				Vec2 pos = new Vec2(boardX, boardY);
				if (!(parent.tileentity.getCircuitData().getPart(pos) instanceof PartNull)) {
					CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(false,
							parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord)
						.add(pos, 0, 0));
				}
			}
		}
	}

	@Override
	public void apply(GuiCAD parent) {
		super.apply(parent);
		currentRotation = 0;
	}
}

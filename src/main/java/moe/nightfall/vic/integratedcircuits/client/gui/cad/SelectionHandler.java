package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.gui.Gui;

public class SelectionHandler extends CADHandler {

	private Vec2 selectionStart = Vec2.zero;
	private Vec2 selectionEnd = Vec2.zero;
	private boolean mouseDown = false;

	private boolean hasSelection() {
		return selectionStart != selectionEnd;
	}

	@Override
	public void render(GuiCAD parent, int mx, int my) {
		// TODO Not convenient, discriminates selection
		if (mouseDown) {
			int gridX = (int) Math.ceil(parent.boardAbs2RelX(mx));
			int gridY = (int) Math.ceil(parent.boardAbs2RelY(my));

			if (gridX >= 0 && gridX < parent.getCircuitData().getSize() && gridY > 0 && gridY < parent.getCircuitData().getSize()) {
				selectionEnd = new Vec2(gridX, gridY);
			}
		}
		
		if (hasSelection()) {
			Gui.drawRect(selectionStart.x, selectionStart.y, selectionEnd.x, selectionEnd.y, 0x550000FF);
			RenderUtils.drawBorder(selectionStart.x, selectionStart.y, selectionEnd.x - selectionStart.x, selectionEnd.y - selectionStart.y);
		}
	}

	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int button) {
		mouseDown = false;
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {

		if (parent.isShiftKeyDown())
			return;

		int gridX = (int) Math.ceil(parent.boardAbs2RelX(mx));
		int gridY = (int) Math.ceil(parent.boardAbs2RelY(my));

		selectionStart = selectionEnd = Vec2.zero;
		if (gridX > 0 && gridX < parent.getCircuitData().getSize() && gridY > 0 && gridY < parent.getCircuitData().getSize()) {
			selectionStart = new Vec2(gridX, gridY);
			mouseDown = true;
		}
	}

	@Override
	public boolean onKeyTyped(GuiCAD parent, int keycode, char ch) {
		// TODO Auto-generated method stub
		return super.onKeyTyped(parent, keycode, ch);
	}

}

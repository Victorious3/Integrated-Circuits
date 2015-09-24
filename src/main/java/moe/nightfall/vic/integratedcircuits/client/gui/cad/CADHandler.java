package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IUIHandler;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import net.minecraft.client.gui.GuiButton;

public abstract class CADHandler implements IUIHandler<GuiCAD> {

	private boolean active;

	public void renderCADCursor(GuiCAD parent, double mouseX, double mouseY, int gridX, int gridY, CircuitData cdata) {
	}

	@Override
	public void render(GuiCAD parent, int mx, int my) {
	}

	@Override
	public void onActionPerformed(GuiCAD parent, GuiButton button) {
	}

	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int button) {
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {
	}

	@Override
	public void onMouseDragged(GuiCAD parent, int mx, int my) {
	}

	@Override
	public boolean onMouseWheel(int amount) {
		return false;
	}

	@Override
	public boolean onKeyTyped(GuiCAD parent, int keycode, char ch) {
		return false;
	}

	protected boolean isActive() {
		return active;
	}

	@Override
	public void apply(GuiCAD parent) {
		active = true;
	}

	@Override
	public void remove(GuiCAD parent) {
		active = false;
	}
}

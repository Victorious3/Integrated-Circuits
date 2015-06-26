package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IUIHandler;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;

public abstract class CADHandler implements IUIHandler<GuiCAD> {

	public void renderCADCursor(GuiCAD parent, double mouseX, double mouseY, int gridX, int gridY, CircuitData cdata) {

	}

	@Override
	public void render(GuiCAD parent, int mx, int my) {

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
	public void apply() {

	}

	@Override
	public void remove() {

	}
}

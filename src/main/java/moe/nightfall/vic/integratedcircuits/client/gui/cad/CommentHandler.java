package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiIconButton;
import moe.nightfall.vic.integratedcircuits.cp.CircuitProperties.Comment;
import net.minecraft.client.gui.GuiButton;

public class CommentHandler extends CADHandler {

	private Comment activeComment;

	@Override
	public void render(GuiCAD parent, int mx, int my) {
		if (isActive()) {

		}
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {
		super.onMouseDown(parent, mx, my, button);
	}

	@Override
	public void remove(GuiCAD parent) {
		super.remove(parent);
		unselect(parent, null);
	}

	public static void unselect(GuiCAD parent, GuiButton selected) {
		// TODO This is ugly
		for (String category : parent.rollover.getCategories()) {
			for (GuiButton button : parent.rollover.getButtons(category)) {
				if (button instanceof GuiIconButton && button != selected) {
					((GuiIconButton) button).setToggled(false);
				}
			}
		}
	}
}

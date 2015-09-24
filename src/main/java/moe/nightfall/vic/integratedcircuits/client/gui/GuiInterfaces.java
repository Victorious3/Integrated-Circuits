package moe.nightfall.vic.integratedcircuits.client.gui;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiCallback;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiCallback.Action;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiInterfaces {
	public interface IHoverable {
		public List<String> getHoverInformation();
	}

	public interface IHoverableHandler {
		public void setCurrentItem(IHoverable hoverable);
	}

	public interface IGuiCallback {
		public void onCallback(GuiCallback gui, Action result, int id);
	}

	public interface IUIHandler<T extends Gui> {

		public void onActionPerformed(T parent, GuiButton button);

		public void onMouseDown(T parent, int mx, int my, int button);

		public void onMouseUp(T parent, int mx, int my, int button);

		public void onMouseDragged(T parent, int mx, int my);

		public boolean onMouseWheel(int amount);

		public boolean onKeyTyped(T parent, int keycode, char ch);

		public void render(T parent, int mx, int my);

		public void apply(T parent);

		public void remove(T parent);
	}
}
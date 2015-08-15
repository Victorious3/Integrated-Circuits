package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.client.Resources;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiInterfaces.IHoverable;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiPartChooser extends GuiButton implements IHoverable {
	public CircuitRenderWrapper current;
	private ArrayList<GuiPartChooser> children;
	public int mode;
	private boolean active = false;
	private boolean showList = false;
	private GuiCAD parent;
	private GuiPartChooser chooserParent;

	public GuiPartChooser(int id, int x, int y, int mode, GuiCAD parent) {
		super(id, x, y, 20, 20, "");
		this.mode = mode;
		this.parent = parent;
	}

	public GuiPartChooser(int id, int x, int y, CircuitRenderWrapper current, GuiCAD parent) {
		this(id, x, y, current, null, parent);
	}

	public GuiPartChooser(int id, int x, int y, CircuitRenderWrapper current, List<CircuitRenderWrapper> list2,
			GuiCAD parent) {
		super(id, x, y, 20, 20, "");
		this.current = current;
		if (list2 != null) {
			ArrayList<CircuitRenderWrapper> list3 = new ArrayList<CircuitRenderWrapper>(list2);
			list3.add(0, current);
			this.children = new ArrayList<GuiPartChooser>();
			for (int i = 0; i < list3.size(); i++) {
				GuiPartChooser child = new GuiPartChooser(i, x - 21, y + i * 21, list3.get(i), parent);
				child.chooserParent = this;
				child.visible = false;
				this.children.add(child);
			}
		}
		mode = 0;
		this.parent = parent;
	}

	public GuiPartChooser(int id, int x, int y, List<CircuitRenderWrapper> list2, GuiCAD parent) {
		super(id, x, y, 20, 20, "");
		if (list2.size() > 0) {
			current = list2.get(0);
			ArrayList<CircuitRenderWrapper> list3 = new ArrayList<CircuitRenderWrapper>(list2);
			this.children = new ArrayList<GuiPartChooser>();
			for (int i = 0; i < list3.size(); i++) {
				GuiPartChooser child = new GuiPartChooser(i, x - 21, y + i * 21, list3.get(i), parent);
				child.chooserParent = this;
				child.visible = false;
				this.children.add(child);
			}
		}
		mode = 0;
		this.parent = parent;
	}

	public GuiPartChooser(int id, int x, int y, CircuitPart.Category category, GuiCAD parent) {
		this(id, x, y, getRenderWrapperParts(category), parent);
	}

	public static List<CircuitRenderWrapper> getRenderWrapperParts(CircuitPart.Category category) {
		return getRenderWrapperParts(CircuitPart.getParts(category));
	}

	public static List<CircuitRenderWrapper> getRenderWrapperParts(List<CircuitPart> parts) {
		ArrayList<CircuitRenderWrapper> renderWrappers = new ArrayList<CircuitRenderWrapper>();
		for (CircuitPart part : parts) {
			Collection<Integer> subtypes = part.getSubtypes();
			if (subtypes == null || subtypes.size() == 0) renderWrappers.add(new CircuitRenderWrapper(part.getClass()));
			else for (int metadata : subtypes)
					renderWrappers.add(new CircuitRenderWrapper(part.getClass(), metadata));
		}
		return renderWrappers;
	}

	@Override
	public void drawButton(Minecraft mc, int x, int y) {
		super.drawButton(mc, x, y);
		mc.getTextureManager().bindTexture(Resources.RESOURCE_PCB);
		if (mode == 0)
			CircuitPartRenderer.renderPart(current, this.xPosition + 2, this.yPosition + 2);
		else
			drawTexturedModalRect(this.xPosition + 2, this.yPosition + 1, (4 + mode) * 16, 15 * 16, 16, 16);

		if (showList && children != null) {
			drawRect(xPosition, yPosition - 1, xPosition + width + 1, yPosition + height + 1, 180 << 24);
			drawRect(xPosition - 22, yPosition - 1, xPosition, yPosition + children.size() * 21, 180 << 24);
			for (GuiPartChooser child : children) {
				child.drawButton(mc, x, y);
			}
		}

		if (x > xPosition && y > yPosition && x < xPosition + width && y < yPosition + height) {
			parent.setCurrentItem(this);
		}
	}

	public GuiPartChooser setActive(boolean active) {
		this.active = active;
		return this;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int x, int y) {
		boolean bool = super.mousePressed(mc, x, y);
		if (showList && children != null) {
			for (GuiPartChooser child : children) {
				if (child.mousePressed(mc, x, y)) {
					child.func_146113_a(mc.getSoundHandler());
					parent.selectedChooser = child;
				}
			}
		}
		if (!bool && children != null) {
			showList = false;
			parent.blockMouseInput = false;
		}
		return bool;
	}

	@Override
	public void mouseReleased(int x, int y) {
		if (!active) {

			if (chooserParent == null) {
				for (Object obj : parent.getButtonList())
					if (obj instanceof GuiPartChooser)
						((GuiPartChooser) obj).setActive(false);
			}

			if (mode == 1) {
				if (parent.getHandler() == parent.placeHandler)
					parent.setHandler(parent.editHandler);
			} else {
				if (parent.getHandler() == parent.editHandler)
					parent.setHandler(parent.placeHandler);
				if (mode == 2)
					parent.placeHandler.selectedPart = new CircuitRenderWrapper(0, CircuitPart.getPart(0));
				else
					parent.placeHandler.selectedPart = current;
			}

			setActive(true);

			if (chooserParent != null) {
				for (GuiPartChooser child : chooserParent.children) {
					child.setActive(false);
				}

				chooserParent.current = this.current;
			}
		}
		if (children != null && children.size() > 1) {
			showList = !showList;
			parent.blockMouseInput = showList;
			for (GuiPartChooser child : children) {
				child.visible = showList;
			}
		}
	}

	@Override
	public int getHoverState(boolean par1) {
		return !enabled ? 0 : active ? 2 : par1 ? 2 : 1;
	}

	@Override
	public List<String> getHoverInformation() {
		ArrayList<String> text = new ArrayList<String>();
		if (current != null)
			text.add(current.getPart().getLocalizedName(current.getPos(), current));
		else if (mode == 1)
			text.add(I18n.format("gui.integratedcircuits.cad.edit"));
		else if (mode == 2)
			text.add(I18n.format("gui.integratedcircuits.cad.erase"));
		return text;
	}
}

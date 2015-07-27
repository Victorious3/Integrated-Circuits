package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiIconButton;
import moe.nightfall.vic.integratedcircuits.client.gui.component.GuiTextArea;
import moe.nightfall.vic.integratedcircuits.cp.CircuitProperties.Comment;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.net.PacketPCBComment;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

public class CommentHandler extends CADHandler {

	// TODO Render this to an FBO for better performance
	// private static Framebuffer fbo;

	private Comment selectedComment;
	private Map<Comment, Vec2> sizeCache = new WeakHashMap<Comment, Vec2>();
	public Mode mode = Mode.EDIT;
	private GuiTextArea textArea = new GuiTextArea(0, 0)
		.setBackgroundColor(0xFFFFFFFF)
		.setTextColor(0xFF000000)
		.setCursorColor(0xFF000000)
		.setVisible(false)
		.setActive(false);

	public static enum Mode {
		EDIT, MOVE, DELETE
	}

	@Override
	public void render(GuiCAD parent, int mx, int my) {
		Comment hovered = getIntersecting(parent, parent.boardAbs2RelX(mx), parent.boardAbs2RelY(my));
		GL11.glPushMatrix();
		GL11.glScalef(1 / 32F, 1 / 32F, 1 / 32F);
		for (Comment comment : parent.getCircuitData().getProperties().getComments()) {
			if (mode == Mode.EDIT && selectedComment == comment) {
				renderEditComment(comment, my, my);
			} else {
				renderComment(comment, hovered == comment);
			}
		}
		GL11.glPopMatrix();
	}

	private void refreshCache(GuiCAD parent) {
		sizeCache.clear();
		for (Comment comment : parent.getCircuitData().getProperties().getComments()) {
			sizeCache.put(comment, calculateSize(comment));
		}
	}

	private Vec2 calculateSize(Comment comment) {
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		String[] text = MiscUtils.stringNewlineSplit(comment.text);
		int width = 10;
		int height = Math.max(fr.FONT_HEIGHT, text.length * fr.FONT_HEIGHT) + 10;

		for (int i = 0; i < text.length; i++) {
			width = Math.max(width, fr.getStringWidth(text[i]));
		}
		width += 10;
		return new Vec2(width, height);
	}

	private Vec2 getSize(Comment comment) {
		Vec2 size = sizeCache.get(comment);
		if (size == null) {
			size = calculateSize(comment);
			sizeCache.put(comment, size);
		}
		return size;
	}

	private Comment getIntersecting(GuiCAD parent, double gridX, double gridY) {
		Collection<Comment> comments = parent.getCircuitData().getProperties().getComments();
		for (Comment comment : Lists.reverse(new ArrayList<Comment>(comments))) {
			Vec2 size = getSize(comment);

			double x = comment.xPos;
			double y = comment.yPos;
			double width = size.x / 32D;
			double height = size.y / 32D;

			if (gridX >= x && gridY >= y && gridX < x + width && gridY < y + height) {
				return comment;
			}
		}
		return null;
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {
		if (parent.isShiftKeyDown())
			return;
		double gridX = parent.boardAbs2RelX(mx);
		double gridY = parent.boardAbs2RelY(my);

		Comment unselectedComment = selectedComment;
		selectedComment = getIntersecting(parent, gridX, gridY);

		// Re-add for topmost
		if (selectedComment != null) {
			parent.getCircuitData().getProperties().removeComment(selectedComment);
			parent.getCircuitData().getProperties().addComment(selectedComment);
		}

		if (selectedComment != null) {
			if (mode == Mode.MOVE) {
				dragRelX = gridX - selectedComment.xPos;
				dragRelY = gridY - selectedComment.yPos;
			} else if (mode == Mode.DELETE) {
				parent.getCircuitData().getProperties().removeComment(selectedComment);
			} else if (mode == Mode.EDIT) {
				if (selectedComment != unselectedComment)
					textArea.setText(selectedComment.text);
			}
		} else {
			if (mode == Mode.EDIT) {
				if (unselectedComment == null) {
					selectedComment = new Comment(gridX, gridY).setText("<Comment>");
					textArea.setText(selectedComment.text);
					textArea.selectAll();
					textArea.setActive(true);
					parent.getCircuitData().getProperties().addComment(selectedComment);
				} else {
					saveText(unselectedComment, parent);
				}
			}
		}

		if (mode == Mode.EDIT) {
			if (selectedComment != null) {
				textArea.onMouseDown((int) ((gridX - selectedComment.xPos) * 32), (int) ((gridY - selectedComment.yPos) * 32), button);
			}
		}
	}

	private void saveText(Comment comment, GuiCAD parent) {
		if (comment == null)
			return;
		comment.setText(textArea.getText());
		sizeCache.remove(comment);
		CommonProxy.networkWrapper.sendToServer(new PacketPCBComment(parent.tileentity.xCoord, parent.tileentity.yCoord, parent.tileentity.zCoord, comment));
	}

	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int button) {
		if (mode == Mode.MOVE) {
			selectedComment = null;
		}
	}

	private double dragRelX, dragRelY;

	@Override
	public void onMouseDragged(GuiCAD parent, int mx, int my) {

		double gridX = parent.boardAbs2RelX(mx);
		double gridY = parent.boardAbs2RelY(my);

		if (mode == Mode.MOVE && selectedComment != null) {
			selectedComment.xPos = gridX - dragRelX;
			selectedComment.yPos = gridY - dragRelY;

			// Snap to grid
			double gridOffsetX = selectedComment.xPos - Math.floor(selectedComment.xPos);
			double gridOffsetY = selectedComment.yPos - Math.floor(selectedComment.yPos);

			if (selectedComment.xPos >= -1 && selectedComment.xPos <= parent.getBoardSize()
					&& selectedComment.yPos >= -1 && selectedComment.yPos <= parent.getBoardSize()) {

				if (gridOffsetX < 3 / 16F) {
					selectedComment.xPos = Math.floor(selectedComment.xPos);
				} else if (gridOffsetX > 13 / 16F) {
					selectedComment.xPos = Math.ceil(selectedComment.xPos);
				}
				if (gridOffsetY < 3 / 16F) {
					selectedComment.yPos = Math.floor(selectedComment.yPos);
				} else if (gridOffsetY > 13 / 16F) {
					selectedComment.yPos = Math.ceil(selectedComment.yPos);
				}
			}
		} else if (mode == Mode.EDIT && selectedComment != null && textArea.isActive()) {
			textArea.onMouseDragged((int) ((gridX - selectedComment.xPos) * 32), (int) ((gridY - selectedComment.yPos) * 32));
		}
	}

	@Override
	public boolean onKeyTyped(GuiCAD parent, int keycode, char ch) {
		if (mode == Mode.EDIT && selectedComment != null) {
			textArea.onKeyTyped(keycode, ch);
			// Unset the comment if enter was pressed
			if (!textArea.isActive()) {
				saveText(selectedComment, parent);
				selectedComment = null;
			}
			return true;
		}
		return false;
	}

	@Override
	public void apply(GuiCAD parent) {
		super.apply(parent);
		refreshCache(parent);
		saveText(selectedComment, parent);
		selectedComment = null;
	}

	@Override
	public void remove(GuiCAD parent) {
		super.remove(parent);
		saveText(selectedComment, parent);
		unselect(parent, null);
	}

	public void renderComment(Comment comment, boolean hovered) {
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int x = (int) (comment.xPos * 32D);
		int y = (int) (comment.yPos * 32D);

		Vec2 size = getSize(comment);
		Gui.drawRect(x, y, x + size.x, y + size.y, hovered || !isActive() || comment == selectedComment ? 0xFFFFFFFF : 0xAAFFFFFF);
		fr.drawSplitString(MiscUtils.stringNormalizeLinefeed(comment.text), x + 5, y + 5, Integer.MAX_VALUE, 0xFF000000);
		drawBorder(x, y, size.x, size.y);
	}

	public void renderEditComment(Comment comment, int mx, int my) {
		int x = (int) (comment.xPos * 32D);
		int y = (int) (comment.yPos * 32D);

		textArea.setVisible(true);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		textArea.render(mx, my);
		GL11.glPopMatrix();
		Vec2 size = textArea.getSize();
		drawBorder(x, y, size.x, size.y);
	}

	private void drawBorder(int x, int y, int width, int height) {
		// Draw line loop
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glColor3f(0, 0, 0);
		GL11.glLineStipple(4, (short) 0xAAAA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + width, y);
		GL11.glVertex2f(x + width, y + height);
		GL11.glVertex2f(x, y + height);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
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

package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/** Specific implementation, grows in size. No automatic line wrapping */
public class GuiTextArea extends Gui {

	private List<StringBuilder> textBuffer = new ArrayList<StringBuilder>();
	private List<Integer> cachedWidth = new ArrayList<Integer>();

	private Vec2 cursorPosition;
	private Vec2 selectionStart;
	private int xCoord, yCoord;
	private int width;
	private boolean active;

	private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

	public GuiTextArea(int xCoord, int yCoord) {
		textBuffer.add(new StringBuilder());
		cachedWidth.add(0);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	public void render(int mx, int my) {
		for (int i = 0; i < textBuffer.size(); i++) {
			// TODO Add tab size setting
			fontRenderer.drawString(textBuffer.get(i).toString().replaceAll("\t", "    "), mx, my + i * fontRenderer.FONT_HEIGHT, 0xFFFFFF);
		}
		// Cursor
		fontRenderer.drawString("|", fontRenderer.getStringWidth(getCurrentLine().substring(0, cursorPosition.x)), my + cursorPosition.y * fontRenderer.FONT_HEIGHT, 0xFFFFFF);
	}

	public void onMouseDown(int mx, int my, int button) {
		Vec2 intersection = intersect(mx, my);
		if (intersection == null) {
			setActive(false);
			return;
		}
		setActive(true);
		setCursorPosition(intersection);
	}

	public void onMouseUp(int mx, int my, int button) {

	}

	public void onMouseDragged(int mx, int my) {

	}

	public void onKeyTyped(int key, char ch) {
		if(!isActive()) return;
		switch (ch) {
			case 1: // ^A
				selectionStart = Vec2.zero;
				setCursorPosition(getLastPosition());
				break;
			case 3: // ^C
				GuiScreen.setClipboardString(getSelectedText());
				break;
			case 22: // ^V
				replace(GuiScreen.getClipboardString());
				break;
			case 24: // ^X
				GuiScreen.setClipboardString(getSelectedText());
				deleteSelected();
				break;
			case 8: // backspace
				backspace();
				break;
			case 127: // delete
				delete();
				break;
			case 27: // ESC
				setActive(false);
				break;
			case 13: // CR
				carriageReturn();
			default:
				replace(String.valueOf(ch));
		}
	}

	private void backspace() {
		if (hasSelection()) {
			deleteSelected();
			return;
		}
		if (cursorPosition.equals(Vec2.zero))
			return;
		if (cursorPosition.x == 0) {
			StringBuilder line = getCurrentLine();
			removeFromCache(cursorPosition.y);
			getLine(cursorPosition.y - 1).append(line);
			refreshLine(cursorPosition.y - 1);
			setCursorPosition(new Vec2(getLineLength(cursorPosition.y - 1) - 1, cursorPosition.y - 1));
		} else {
			getCurrentLine().deleteCharAt(cursorPosition.x - 1);
			refreshLine(cursorPosition.y);
			refreshWidth();
			setCursorPosition(new Vec2(cursorPosition.x - 1, cursorPosition.y));
		}
	}

	private void delete() {
		if (hasSelection()) {
			deleteSelected();
			return;
		}
		if (cursorPosition.equals(getLastPosition()))
			return;
		if (cursorPosition.x >= getLineLength(cursorPosition.y) - 1) {
			StringBuilder line = getLine(cursorPosition.y + 1);
			removeFromCache(cursorPosition.y + 1);
			getLine(cursorPosition.y).append(line);
			refreshLine(cursorPosition.y);
		} else {
			getCurrentLine().deleteCharAt(cursorPosition.x);
			refreshLine(cursorPosition.y);
			refreshWidth();
		}
	}

	private void carriageReturn() {
		if (GuiScreen.isShiftKeyDown()) {
			deleteSelected();

			StringBuilder sb = new StringBuilder();
			addToCache(cursorPosition.y, sb);
			String currentLine = getCurrentLine().toString();
			if (currentLine.length() > 0) {
				// Add tabs in front for indent
				int i = 0;
				int size = currentLine.length();
				while (currentLine.charAt(i) == '\t' && i < size) {
					sb.insert(0, "\n");
					i++;
				}
			}
			setCursorPosition(new Vec2(getLineLength(cursorPosition.y + 1) - 1, cursorPosition.y + 1));
			sb.append(currentLine.substring(cursorPosition.x, currentLine.length() - 1));
		} else {
			// un-select
			setActive(false);
		}
	}

	private Vec2 intersect(int mx, int my) {
		int y = (my - xCoord) / fontRenderer.FONT_HEIGHT;
		if (y < 0 || y > textBuffer.size())
			return null;
		int x = mx - xCoord;
		if (x < 0)
			return null;
		if (x > width)
			return null;
		String s = fontRenderer.trimStringToWidth(textBuffer.get(x).toString(), mx - xCoord);
		x = s.length() - 1;
		return new Vec2(x, y);
	}

	private StringBuilder getLine(int line) {
		return textBuffer.get(line);
	}

	private StringBuilder getCurrentLine() {
		return getLine(cursorPosition.y);
	}

	private void refreshLine(int line) {
		cachedWidth.set(line, fontRenderer.getStringWidth(textBuffer.get(line).toString()));
	}

	private void refreshWidth() {
		width = 0;
		for (int w : cachedWidth) {
			if (w > width)
				width = w;
		}
	}

	private void addToCache(int line, StringBuilder sb) {
		textBuffer.add(line, sb);
		cachedWidth.add(line, 0);
		refreshLine(line);
		refreshWidth();
	}

	private void addToCache(StringBuilder sb) {
		textBuffer.add(sb);
		cachedWidth.add(0);
		refreshLine(textBuffer.size() - 1);
		refreshWidth();
	}

	private void removeFromCache(int line) {
		textBuffer.remove(line);
		cachedWidth.remove(line);
		refreshWidth();
	}

	public int getLineLength(int line) {
		return textBuffer.get(line - 1).length();
	}

	public Vec2 getLastPosition() {
		return new Vec2(textBuffer.get(textBuffer.size() - 1).length() - 1, textBuffer.size() - 1);
	}

	public Vec2 getSelectionStart() {
		return selectionStart;
	}

	public Vec2 getCursorPosition() {
		return cursorPosition;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	private Pair<Vec2, Vec2> selection() {
		Vec2 first, last;
		if (selectionStart.y < cursorPosition.y || (selectionStart.y == cursorPosition.y && selectionStart.x < selectionStart.y)) {
			first = selectionStart;
			last = cursorPosition;
		} else {
			first = cursorPosition;
			last = selectionStart;
		}
		return new ImmutablePair<Vec2, Vec2>(first, last);
	}

	public String getSelectedText() {
		if (!hasSelection())
			return "";

		Pair<Vec2, Vec2> selection = selection();
		Vec2 first = selection.getLeft();
		Vec2 last = selection.getRight();

		List<StringBuilder> selectedLines = textBuffer.subList(first.y, last.y);
		if (selectedLines.size() == 1) {
			return selectedLines.get(0).substring(first.x, last.x);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(selectedLines.get(0).substring(first.x));
			for (int i = 0; i < selectedLines.size() - 2; i++) {
				sb.append(selectedLines.get(i));
			}
			sb.append(selectedLines.get(selectedLines.size() - 1).substring(0, last.x));
			return sb.toString();
		}
	}

	public boolean hasSelection() {
		return !selectionStart.equals(cursorPosition);
	}

	public void clearSelection() {
		selectionStart = cursorPosition;
	}

	public void deleteSelected() {
		if (!hasSelection())
			return;
		Pair<Vec2, Vec2> selection = selection();
		Vec2 first = selection.getLeft();
		Vec2 last = selection.getRight();

		StringBuilder firstLine = textBuffer.get(first.y);
		firstLine.delete(first.x, last.x);
		refreshLine(first.y);
		if (first.y == last.y) {
			refreshWidth();
			return;
		}

		List<StringBuilder> selectedLines = textBuffer.subList(first.y + 1, last.y);
		StringBuilder lastLine = selectedLines.get(selectedLines.size() - 1);
		if (last.x < lastLine.length() - 1) {
			firstLine.append(lastLine.substring(last.x, lastLine.length() - 1));
			refreshLine(first.y);
		}
		if (selectedLines.size() > 1) {
			for (int i = 1; i < selectedLines.size() - 1; i++) {
				textBuffer.remove(first.y + i);
			}
		}
		setCursorPositionInternal(first);
		refreshWidth();
	}

	public void append(String text) {
		insert(text, getLastPosition());
	}

	public void insert(String text) {
		// TODO Advance cursor
		insert(text, cursorPosition);
	}

	public void insert(String text, Vec2 pos) {
		clearSelection();
		if (text == null)
			throw new NullPointerException();
		checkInside(pos);
		String[] toInsert = MiscUtils.stringNewlineSplit(text);
		StringBuilder insertLine = textBuffer.get(pos.y);
		String endOfLine = insertLine.substring(pos.x, insertLine.length() - 1);
		insertLine.delete(pos.x, insertLine.length() - 1);
		insertLine.append(toInsert[0]);
		refreshLine(pos.y);

		if (toInsert.length > 2) {
			for (int i = 1; i < toInsert.length - 2; i++) {
				textBuffer.add(pos.y + i - 1, new StringBuilder(toInsert[i]));
			}
		}
		if (toInsert.length > 1) {
			textBuffer.add(new StringBuilder(toInsert[toInsert.length - 1]).append(endOfLine));
		}
		refreshWidth();
	}

	public void replace(String text) {
		deleteSelected();
		insert(text);
	}

	public void setCursorPosition(Vec2 position) {
		if (position.x < 0 || position.y < 0)
			throw new IndexOutOfBoundsException();
		if (position.y > textBuffer.size()) {
			position = getLastPosition();
		} else if (position.x >= getLineLength(position.y)) {
			position = new Vec2(getLineLength(position.y) - 1, position.y);
		}
		setCursorPositionInternal(position);
	}

	private void setCursorPositionInternal(Vec2 position) {
		this.cursorPosition = position;
		this.selectionStart = position;
	}

	public GuiTextArea setText(String text) {
		setCursorPosition(Vec2.zero);
		textBuffer.clear();
		cachedWidth.clear();
		addToCache(new StringBuilder());
		append(text);
		return this;
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < textBuffer.size(); i++) {
			sb.append(textBuffer.get(i));
			if (i != textBuffer.size() - 1) {
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	private void checkInside(Vec2 pos) {
		if (pos.x < 0 || pos.y < 0 || pos.y >= textBuffer.size())
			throw new IndexOutOfBoundsException();
		if (pos.x >= getLineLength(pos.y))
			throw new IndexOutOfBoundsException();
	}
}

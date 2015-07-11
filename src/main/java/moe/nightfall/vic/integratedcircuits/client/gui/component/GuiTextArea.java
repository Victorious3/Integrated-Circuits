package moe.nightfall.vic.integratedcircuits.client.gui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Specific implementation, grows in size. No automatic line wrapping */
public class GuiTextArea extends Gui {

	private List<StringBuilder> textBuffer = new ArrayList<StringBuilder>();
	private List<Integer> cachedWidth = new ArrayList<Integer>();

	protected Vec2 cursorPosition;
	protected Vec2 selectionStart;
	protected int xCoord, yCoord;
	protected int width;

	protected boolean active = true;
	protected boolean visible = true;

	protected int backgroundColor;
	protected int borderColor;
	protected int textColor = 0xFFFFFFFF;
	protected int cursorColor = 0xFFFFFFFF;

	protected FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

	public GuiTextArea(int xCoord, int yCoord) {
		textBuffer.add(new StringBuilder());
		cachedWidth.add(0);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	public void render(int mx, int my) {

		if (!isVisible())
			return;

		if (backgroundColor != 0) {
			drawRect(xCoord, yCoord, xCoord + width, yCoord + textBuffer.size() * fontRenderer.FONT_HEIGHT, backgroundColor);
		}

		if (isActive() && hasSelection()) {
			renderSelection(0xFFDFB578);
		}

		// Called to reset the styles
		RenderUtils.resetColors(fontRenderer, isActive() ? textColor : 0xFF888888);
		for (int i = 0; i < textBuffer.size(); i++) {
			RenderUtils.drawStringNoReset(fontRenderer, textBuffer.get(i).toString(), xCoord, yCoord + i * fontRenderer.FONT_HEIGHT, false);
		}

		// Selection
		if (isActive() && hasSelection()) {
			GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO);
			renderSelection(0xFFFFFFFF);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		// Cursor
		if (isActive() && ClientProxy.clientTicks / 6 % 2 == 0) {
			int cursorX = xCoord + fontRenderer.getStringWidth(getCurrentLine().substring(0, cursorPosition.x));
			int cursorY = yCoord + cursorPosition.y * fontRenderer.FONT_HEIGHT;

			drawVerticalLine(cursorX, cursorY - 2, cursorY + 10, cursorColor);
		}
	}

	private void renderSelection(int color) {
		Pair<Vec2, Vec2> selection = selection();
		Vec2 first = selection.getLeft();
		Vec2 last = selection.getRight();
		if (first.y == last.y) {
			int x1 = fontRenderer.getStringWidth(getLine(first.y).substring(0, first.x));
			int x2 = fontRenderer.getStringWidth(getLine(first.y).substring(0, last.x));
			RenderUtils.drawRect(xCoord + x1, yCoord + first.y * fontRenderer.FONT_HEIGHT, xCoord + x2, yCoord + (first.y + 1) * fontRenderer.FONT_HEIGHT, color);
		} else {
			int x1 = fontRenderer.getStringWidth(getLine(first.y).substring(0, first.x));
			int x2 = cachedWidth.get(first.y);
			RenderUtils.drawRect(xCoord + x1, yCoord + first.y * fontRenderer.FONT_HEIGHT, xCoord + x2, yCoord + (first.y + 1) * fontRenderer.FONT_HEIGHT, color);
			for (int i = 0; i < last.y - first.y - 1; i++) {
				x2 = cachedWidth.get(first.y + i + 1);
				RenderUtils.drawRect(xCoord, yCoord + (first.y + i + 1) * fontRenderer.FONT_HEIGHT, xCoord + x2, yCoord + (first.y + i + 2) * fontRenderer.FONT_HEIGHT, color);
			}

			x2 = fontRenderer.getStringWidth(getLine(last.y).substring(0, last.x));
			RenderUtils.drawRect(xCoord, yCoord + last.y * fontRenderer.FONT_HEIGHT, xCoord + x2, yCoord + (last.y + 1) * fontRenderer.FONT_HEIGHT, color);
		}
	}

	private int clickCount = 0;
	private long lastClickTime = Long.MAX_VALUE;
	private static final int CLICK_TIME = 200;

	public void onMouseDown(int mx, int my, int button) {

		if (!isVisible() || button != 0)
			return;

		Vec2 intersection = intersect(mx, my);
		if (intersection == null) {
			setActive(false);
			return;
		}

		long time = System.currentTimeMillis();
		if (time - lastClickTime > CLICK_TIME) {
			clickCount = 0;
		}
		lastClickTime = time;

		setActive(true);

		if (clickCount == 0) {
			setCursorPositionInternal(intersection);
		} else if (clickCount == 1) {
			selectWord(intersection);
		} else if (clickCount == 2) {
			selectLine(intersection);
			clickCount = 0;
		}

		clickCount++;
	}

	private static final Pattern patternWord = Pattern.compile("\\b");

	private void selectWord(Vec2 intersection) {
		StringBuilder line = getLine(intersection.y);

		Matcher forward = patternWord.matcher(line);
		Matcher backward = patternWord.matcher(new StringBuilder(line).reverse());

		int next = forward.find(intersection.x) ? forward.start() : getLineLength(intersection.y);
		int prev = backward.find(line.length() - intersection.x) ? line.length() - backward.start() : 0;

		selectionStart = new Vec2(prev, intersection.y);
		cursorPosition = new Vec2(next, intersection.y);
	}

	private void selectLine(Vec2 intersection) {
		selectionStart = new Vec2(0, intersection.y);
		if (intersection.y + 1 >= textBuffer.size()) {
			cursorPosition = new Vec2(getLineLength(intersection.y), intersection.y);
		} else {
			cursorPosition = new Vec2(0, intersection.y + 1);
		}
	}

	public void onMouseDragged(int mx, int my) {
		if (!isVisible() || !isActive())
			return;

		Vec2 intersection = intersect(mx, my);
		if (intersection != null) {
			cursorPosition = intersection;
		}
	}

	public void onKeyTyped(int key, char ch) {
		if (!isVisible() || !isActive())
			return;

		switch (key) {
			case Keyboard.KEY_DELETE:
				delete();
				return;
			case Keyboard.KEY_UP:
				up();
				return;
			case Keyboard.KEY_DOWN:
				down();
				return;
			case Keyboard.KEY_LEFT:
				left();
				return;
			case Keyboard.KEY_RIGHT:
				right();
				return;
		}

		switch (ch) {
			case 1: // ^A
				selectionStart = Vec2.zero;
				cursorPosition = getLastPosition();
				return;
			case 3: // ^C
				GuiScreen.setClipboardString(getSelectedText());
				return;
			case 22: // ^V
				replace(GuiScreen.getClipboardString());
				return;
			case 24: // ^X
				GuiScreen.setClipboardString(getSelectedText());
				deleteSelected();
				return;
			case 8: // backspace
				backspace();
				return;
			case 27: // ESC
				setActive(false);
				return;
			case 13: // CR
				carriageReturn();
				return;
			case '\t':
				tab();
				return;
			default:
				if (!Character.isISOControl(ch)) {
					replace(String.valueOf(ch));
				}
				return;
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
			Vec2 newCursorPosition = new Vec2(getLineLength(cursorPosition.y - 1), cursorPosition.y - 1);
			getLine(cursorPosition.y - 1).append(line);
			refreshLine(cursorPosition.y - 1);
			setCursorPositionInternal(newCursorPosition);
			refreshWidth();
		} else {
			getCurrentLine().deleteCharAt(cursorPosition.x - 1);
			refreshLine(cursorPosition.y);
			setCursorPositionInternal(new Vec2(cursorPosition.x - 1, cursorPosition.y));
			refreshWidth();
		}
	}

	private void delete() {
		if (hasSelection()) {
			deleteSelected();
			return;
		}
		if (cursorPosition.equals(getLastPosition()))
			return;
		if (cursorPosition.x >= getLineLength(cursorPosition.y)) {
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
			String currentLine = getCurrentLine().toString();
			if (currentLine.length() > 0) {
				// Add spaces in front for indent
				int i = 0;
				int size = currentLine.length();
				while (currentLine.charAt(i) == ' ' && i < size) {
					sb.insert(0, ' ');
					i++;
				}
			}

			addToCache(cursorPosition.y + 1, sb);
			Vec2 newCursorPosition = new Vec2(getLineLength(cursorPosition.y + 1), cursorPosition.y + 1);
			sb.append(currentLine.substring(cursorPosition.x, currentLine.length()));
			getCurrentLine().delete(cursorPosition.x, currentLine.length());

			refreshLine(cursorPosition.y);
			refreshLine(cursorPosition.y + 1);
			refreshWidth();

			setCursorPositionInternal(newCursorPosition);
		} else {
			// un-select
			setActive(false);
		}
	}

	private static final Pattern patternBlankSpace = Pattern.compile("^\\s*$");
	private static final Pattern patternFirstNonWhitespace = Pattern.compile("[^\\s]|\\s$");

	private void tab() {
		
		if (GuiScreen.isShiftKeyDown()) {
			// Got one line
			if (cursorPosition.y == selectionStart.y) {
				clearSelection();
				if (patternBlankSpace.matcher(getCurrentLine().toString().substring(0, cursorPosition.x)).matches()) {

					// We're starting with some whitespace, move forward
					Matcher matcher = patternFirstNonWhitespace.matcher(getCurrentLine());
					if (matcher.find()) {
						setCursorPositionInternal(new Vec2(matcher.start(), cursorPosition.y));
					}

					// Remove some spaces
					if (cursorPosition.x == 1) {
						// Got one to remove...
						getCurrentLine().deleteCharAt(cursorPosition.x - 1);
						setCursorPositionInternal(new Vec2(cursorPosition.x - 1, cursorPosition.y));
					} else if (cursorPosition.x > 0) {
						// Got two to remove!
						getCurrentLine().delete(cursorPosition.x - 2, cursorPosition.x);
						setCursorPositionInternal(new Vec2(cursorPosition.x - 2, cursorPosition.y));
					}
					refreshLine(cursorPosition.y);
				} else {
					// Move the cursor back
					setCursorPositionInternal(new Vec2(Math.max(cursorPosition.x - 2 + cursorPosition.x % 2, 0), cursorPosition.y));
				}
			} else {
				Pair<Vec2, Vec2> selection = selection();
				// Tab multiple lines
				for (int i = selection.getLeft().y; i <= selection().getRight().y; i++) {
					String line = getLine(i).toString();
					if (line.startsWith("  ")) {
						getLine(i).delete(0, 2);
						refreshLine(i);
					} else if (line.startsWith(" ")) {
						getLine(i).deleteCharAt(0);
						refreshLine(i);
					}
				}

				// Adjust selection
				tab_adjustSelection(selection);
			}
		} else {
			// Got one line
			if (cursorPosition.y == selectionStart.y) {
				if (cursorPosition.x % 2 == 0)
					replace("  ");
				else
					replace(" ");

				if (patternBlankSpace.matcher(getCurrentLine().toString().substring(0, cursorPosition.x)).matches()) {
					// We're starting with some whitespace, move forward
					Matcher matcher = patternFirstNonWhitespace.matcher(getCurrentLine());
					if (matcher.find()) {
						setCursorPositionInternal(new Vec2(matcher.start(), cursorPosition.y));
					}
				}
			} else {
				Pair<Vec2, Vec2> selection = selection();
				// Tab multiple lines
				for (int i = selection.getLeft().y; i <= selection().getRight().y; i++) {
					getLine(i).insert(0, "  ");
					refreshLine(i);
				}

				// Adjust selection
				tab_adjustSelection(selection);
			}
		}
		refreshWidth();
	}

	private void tab_adjustSelection(Pair<Vec2, Vec2> selection) {
		boolean flip = cursorPosition == selection.getLeft();
		selectionStart = new Vec2(0, selection.getLeft().y);
		cursorPosition = new Vec2(getLineLength(selection.getRight().y), selection.getRight().y);

		if (flip) {
			Vec2 temp = selectionStart;
			selectionStart = cursorPosition;
			cursorPosition = temp;
		}
	}

	private void up() {
		if (cursorPosition.y == 0)
			return;

		int x = fontRenderer.getStringWidth(getCurrentLine().substring(0, cursorPosition.x));
		x = fontRenderer.trimStringToWidth(getLine(cursorPosition.y - 1).toString(), x).length();
		setCursorPositionInternal(new Vec2(x, cursorPosition.y - 1));
	}

	private void down() {
		if (cursorPosition.y >= textBuffer.size() - 1)
			return;

		int x = fontRenderer.getStringWidth(getCurrentLine().substring(0, cursorPosition.x));
		x = fontRenderer.trimStringToWidth(getLine(cursorPosition.y + 1).toString(), x).length();
		setCursorPositionInternal(new Vec2(x, cursorPosition.y + 1));
	}

	private void left() {
		if (cursorPosition.equals(Vec2.zero))
			return;
		if (cursorPosition.x == 0) {
			setCursorPositionInternal(new Vec2(getLineLength(cursorPosition.y - 1), cursorPosition.y - 1));
		} else {
			setCursorPositionInternal(new Vec2(cursorPosition.x - 1, cursorPosition.y));
		}
	}

	private void right() {
		if (cursorPosition.equals(getLastPosition()))
			return;
		if (cursorPosition.x >= getLineLength(cursorPosition.y)) {
			setCursorPositionInternal(new Vec2(0, cursorPosition.y + 1));
		} else {
			setCursorPositionInternal(new Vec2(cursorPosition.x + 1, cursorPosition.y));
		}
	}

	private Vec2 intersect(int mx, int my) {
		int y = (my - xCoord) / fontRenderer.FONT_HEIGHT;
		if (y < 0 || y >= textBuffer.size())
			return null;
		int x = mx - xCoord;
		if (x < 0)
			return null;
		if (x > width)
			return null;
		String s = fontRenderer.trimStringToWidth(textBuffer.get(y).toString(), mx - xCoord);
		x = s.length();
		return new Vec2(x, y);
	}

	protected StringBuilder getLine(int line) {
		return textBuffer.get(line);
	}

	protected StringBuilder getCurrentLine() {
		return getLine(cursorPosition.y);
	}

	protected void refreshLine(int line) {
		cachedWidth.set(line, fontRenderer.getStringWidth(textBuffer.get(line).toString()));
	}

	protected void refreshWidth() {
		width = 0;
		for (int w : cachedWidth) {
			if (w > width)
				width = w;
		}
	}

	protected void addToCache(int line, StringBuilder sb) {
		textBuffer.add(line, sb);
		cachedWidth.add(line, 0);
		refreshLine(line);
		refreshWidth();
	}

	protected void addToCache(StringBuilder sb) {
		textBuffer.add(sb);
		cachedWidth.add(0);
		refreshLine(textBuffer.size() - 1);
		refreshWidth();
	}

	protected void removeFromCache(int line) {
		textBuffer.remove(line);
		cachedWidth.remove(line);
		refreshWidth();
	}

	public int getLineLength(int line) {
		return textBuffer.get(line).length();
	}

	public Vec2 getLastPosition() {
		return new Vec2(textBuffer.get(textBuffer.size() - 1).length(), textBuffer.size() - 1);
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

	public boolean isVisible() {
		return visible;
	}

	public GuiTextArea setActive(boolean active) {
		this.active = active;
		return this;
	}

	public GuiTextArea setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public GuiTextArea setPosition(int x, int y) {
		this.xCoord = x;
		this.yCoord = y;
		return this;
	}

	public GuiTextArea setBackgroundColor(int color) {
		this.backgroundColor = color;
		return this;
	}

	public GuiTextArea setBorderColor(int color) {
		this.borderColor = color;
		return this;
	}

	public GuiTextArea setTextColor(int color) {
		this.textColor = color;
		return this;
	}

	public GuiTextArea setCursorColor(int color) {
		this.cursorColor = color;
		return this;
	}

	protected Pair<Vec2, Vec2> selection() {
		Vec2 first, last;
		if (selectionStart.y < cursorPosition.y || (selectionStart.y == cursorPosition.y && selectionStart.x < cursorPosition.x)) {
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

		List<StringBuilder> selectedLines = textBuffer.subList(first.y, last.y + 1);
		if (selectedLines.size() == 1) {
			return selectedLines.get(0).substring(first.x, last.x);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(selectedLines.get(0).substring(first.x)).append("\r\n");
			for (int i = 1; i < selectedLines.size() - 1; i++) {
				sb.append(selectedLines.get(i)).append("\r\n");
			}
			sb.append(selectedLines.get(selectedLines.size() - 1).substring(0, last.x));
			return sb.toString();
		}
	}

	public boolean hasSelection() {
		return !selectionStart.equals(cursorPosition);
	}

	public void clearSelection() {
		selectionStart = selection().getLeft();
	}

	public void deleteSelected() {
		if (!hasSelection())
			return;

		Pair<Vec2, Vec2> selection = selection();
		Vec2 first = selection.getLeft();
		Vec2 last = selection.getRight();

		StringBuilder firstLine = textBuffer.get(first.y);
		if (first.y == last.y) {
			firstLine.delete(first.x, last.x);
		} else {
			firstLine.delete(first.x, getLineLength(first.y));
		}

		refreshLine(first.y);
		setCursorPositionInternal(first);

		if (first.y == last.y) {
			refreshWidth();
			return;
		}

		int numLines = last.y - first.y;
		StringBuilder lastLine = getLine(first.y + numLines);
		if (last.x < lastLine.length() - 1) {
			firstLine.append(lastLine.substring(last.x, lastLine.length()));
			refreshLine(first.y);
		}

		for (int i = 0; i < numLines; i++) {
			removeFromCache(first.y + 1);
		}
	}

	public void append(String text) {
		insert(text, getLastPosition());
	}

	public void insert(String text) {
		insert(text, cursorPosition, true);
	}

	public void insert(String text, Vec2 pos) {
		insert(text, cursorPosition, false);
	}

	protected void insert(String text, Vec2 pos, boolean advanceCursor) {
		clearSelection();
		if (text == null)
			throw new NullPointerException();
		checkInside(pos);

		String[] toInsert = MiscUtils.stringNewlineSplit(text.replaceAll("\t", "  "));
		StringBuilder insertLine = getLine(pos.y);
		String endOfLine = insertLine.substring(pos.x, insertLine.length());
		insertLine.delete(pos.x, insertLine.length());
		insertLine.append(toInsert[0]);

		if (toInsert.length > 1) {

			if (toInsert.length > 2) {
				for (int i = 1; i < toInsert.length - 1; i++) {
					addToCache(new StringBuilder(toInsert[i]));
				}
			}

			addToCache(new StringBuilder(toInsert[toInsert.length - 1]).append(endOfLine));

			if (advanceCursor) {
				// TODO min shouldn't be needed but I've seen this crashing
				// randomly
				int newCursorY = cursorPosition.y + toInsert.length - 1;
				setCursorPositionInternal(new Vec2(Math.min(toInsert[toInsert.length - 1].length(), getLineLength(newCursorY)), newCursorY));
			}
		} else {
			if (advanceCursor)
				setCursorPositionInternal(new Vec2(Math.min(cursorPosition.x + toInsert[0].length(), getLineLength(cursorPosition.y)), cursorPosition.y));
			insertLine.append(endOfLine);
		}

		refreshLine(pos.y);
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
		} else if (position.x > getLineLength(position.y)) {
			position = new Vec2(getLineLength(position.y), position.y);
		}
		setCursorPositionInternal(position);
	}

	protected void setCursorPositionInternal(Vec2 position) {
		this.cursorPosition = position;
		this.selectionStart = position;
	}

	public GuiTextArea setText(String text) {
		setCursorPositionInternal(Vec2.zero);
		textBuffer.clear();
		cachedWidth.clear();
		addToCache(new StringBuilder());
		append(text);
		setCursorPositionInternal(getLastPosition());
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

	protected void checkInside(Vec2 pos) {
		if (pos.x < 0 || pos.y < 0 || pos.y >= textBuffer.size())
			throw new IndexOutOfBoundsException();
		if (pos.x > getLineLength(pos.y))
			throw new IndexOutOfBoundsException();
	}
}


package azure.lyt;

import javax.microedition.lcdui.Image;

import lejos.nxt.LCD;

public class LayoutGraphics {

	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;

	public static LayoutGraphics INSTANCE = new LayoutGraphics();

	public static final int ANCHOR_TOP_LEFT = TOP | LEFT;

	private byte[] fb = LCD.getDisplay();
	private int screenW = LCD.SCREEN_WIDTH;
	private int screenH = LCD.SCREEN_HEIGHT;
	private int transX = 0;
	private int transY = 0;

	private LayoutFont defaultFont = null;

	private LayoutGraphics() {

	}

	public void setDefaultFont(LayoutFont font) {
		this.defaultFont = font;
	}

	private LayoutFont getDefaultFont() {
		if (defaultFont == null) {
			//Fallback to leJOS system font
			defaultFont = new LayoutFont(
				new Image(
					LCD.FONT_WIDTH * LCD.NOOF_CHARS,
					LCD.FONT_HEIGHT,
					LCD.getSystemFont()),
				LCD.FONT_WIDTH,
				LCD.CELL_WIDTH,
				0,
				LCD.NOOF_CHARS
			);
		}
		return defaultFont;
	}

	/**
	 * Adjust the x co-ordinate to use the translation and anchor values.
	 *
	 * @param x
	 *            Original value
	 * @param w
	 *            width of the item.
	 * @param anchor
	 *            anchor parameter
	 * @return updated x value.
	 */
	private int adjustX(int x, int w, int anchor) {
		x += transX;
		switch (anchor & (LEFT | RIGHT | HCENTER)) {
		case LEFT:
			break;
		case RIGHT:
			x -= w;
			break;
		case HCENTER:
			x -= (w >> 1);
			break;
		}
		return x;
	}

	/**
	 * Adjust the y co-ordinate to use the translation and anchor values.
	 *
	 * @param y
	 *            Original value
	 * @param h
	 *            height of the item.
	 * @param anchor
	 *            anchor parameter
	 * @return updated y value.
	 */
	private int adjustY(int y, int h, int anchor) {
		y += transY;
		switch (anchor & (TOP | BOTTOM | VCENTER)) {
		case TOP:
			break;
		case BOTTOM:
			y -= h;
			break;
		case VCENTER:
			y -= (h >> 1);
			break;
		}
		return y;
	}

	public synchronized void translate(int x, int y) {
		transX += x;
		transY += y;
	}

	public void drawImage(Image src, int x, int y, int anchor) {
		drawRegionRop(src, 0, 0, src.getWidth(), src.getHeight(), x, y, anchor, LCD.ROP_COPY);
	}

	public void drawRegion(Image src, int sx, int sy, int w, int h, int x, int y, int anchor) {
		drawRegionRop(src, sx, sy, w, h, x, y, anchor, LCD.ROP_COPY);
	}

	public void drawRegionRop(Image src, int sx, int sy, int w, int h, int x, int y, int anchor, int rop) {
		x = adjustX(x, w, anchor);
		y = adjustY(y, h, anchor);
		if (src == null)
			LCD.bitBlt(fb, screenW, screenH, sx, sy, fb, screenW, screenH, x, y, w, h, rop);
		else
			LCD.bitBlt(src.getData(), src.getWidth(), src.getHeight(), sx, sy, fb, screenW, screenH, x, y, w, h, rop);
	}

	public void drawString(String str, int x, int y, int anchor) {
		drawString(null, str, x, y, anchor);
	}

	public void drawString(LayoutFont font, String str, int x, int y, int anchor) {
		if (font == null) {
			font = getDefaultFont();
		}
		int gw = font.glyphWidth;
		int gh = font.fontSize;
		int rop = LCD.ROP_COPY;
		int cellWidth = font.cellWidth;
		int rasterW = gw * font.charaCount;
		char[] strData = str.toCharArray();
		// let's assemble a single image of the whole text...
		for (int i = 0; i < strData.length; i++) {
			LCD.bitBlt(font.buf, rasterW, gh, gw * (strData[i] - font.charaOffset), 0, fb, 100, 64, x + i * cellWidth,
					y, gw, gh, rop);
		}
	}
}

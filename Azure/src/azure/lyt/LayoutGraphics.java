
package azure.lyt;

import javax.microedition.lcdui.Graphics;

import azure.common.ResourceManager;
import lejos.nxt.LCD;

public class LayoutGraphics extends Graphics {

	public static LayoutGraphics g = new LayoutGraphics();

	public static final int ANCHOR_TOP_LEFT = Graphics.TOP | Graphics.LEFT;

	@Override
	public void drawString(String str, int x, int y, int anchor) {
		drawString(ResourceManager.getDefaultFont(), str, x, y, anchor);
	}

	public void drawString(LayoutFont font, String str, int x, int y, int anchor) {
		int gw = font.glyphWidth;
		int gh = font.fontSize;
		int rop = LCD.ROP_COPY;
		int cellWidth = font.cellWidth;
		int rasterW = gw * font.charaCount;
		char[] strData = str.toCharArray();
		byte[] disp = LCD.getDisplay();
		// let's assemble a single image of the whole text...
		for (int i = 0; i < strData.length; i++) {
            LCD.bitBlt(
            		font.buf,
            		rasterW,
            		gh,
            		gw * (strData[i] - font.charaOffset),
            		0,
            		disp,
            		100,
            		64,
            		x + i * cellWidth,
            		y,
            		gw,
            		gh,
            		rop
            );
		}
	}
}

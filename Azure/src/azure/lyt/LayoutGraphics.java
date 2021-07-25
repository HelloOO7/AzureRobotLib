


package azure.lyt;

import azure.common.ResourceManager;
import javax.microedition.lcdui.Graphics;

import lejos.nxt.LCD;

public class LayoutGraphics extends Graphics{

	public static LayoutGraphics g = new LayoutGraphics();

	public static final int ANCHOR_TOP_LEFT = Graphics.TOP | Graphics.LEFT;

	public void drawString(String str, int x, int y, int anchor)
    {
		LayoutFont font = ResourceManager.getDefaultFont();
        int gw = font.glyphWidth;
        int rop = LCD.ROP_COPY;
        int cellWidth = 6;
        char[] strData = str.toCharArray();
        //let's assemble a single image of the whole text...
        byte[] idata = new byte[cellWidth*strData.length];
        for (int i = 0; i < strData.length; i++) {
        	System.arraycopy(font.getCharacterRaster(strData[i]), 0, idata, i*cellWidth, gw);
        }
        LCD.bitBlt(idata, idata.length, font.fontSize, 0, 0, LCD.getDisplay(), 100, 64, x, y, idata.length, font.fontSize, rop);
    }
}

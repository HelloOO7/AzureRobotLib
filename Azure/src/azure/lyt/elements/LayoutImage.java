package azure.lyt.elements;

import javax.microedition.lcdui.Image;

import azure.lyt.LayoutGraphics;
import azure.common.ResourceManager;

public class LayoutImage extends ILayoutDrawable{
	private Image img;

	public LayoutImage(String resource) {
		img = ResourceManager.readImage(resource);
	}

	public LayoutImage(Image img) {
		this.img = img;
	}


	@Override
	public void draw() {
		LayoutGraphics.g.drawImage(img, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
	}
}

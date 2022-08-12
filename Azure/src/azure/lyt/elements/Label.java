package azure.lyt.elements;

import azure.common.ResourceManager;
import azure.lyt.LayoutFont;
import azure.lyt.LayoutGraphics;

public class Label extends ILayoutDrawable{
	private String text;
	private LayoutFont font;

	public Label(String text){
		this(text, ResourceManager.getDefaultFont());
	}

	public Label(String text, LayoutFont font){
		setText(text);
		this.font = font;
	}

	public void setText(String text){
		this.text = text;
	}

	@Override
	public void draw() {
		LayoutGraphics.g.drawString(font, text, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
	}
}

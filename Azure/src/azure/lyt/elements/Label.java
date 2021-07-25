package azure.lyt.elements;

import azure.lyt.LayoutGraphics;

public class Label extends ILayoutDrawable{
	private String text;

	public Label(String text){
		setText(text);
	}

	public void setText(String text){
		this.text = text;
	}

	@Override
	public void draw() {
		LayoutGraphics.g.drawString(text, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
	}
}

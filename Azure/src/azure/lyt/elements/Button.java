
package azure.lyt.elements;

import javax.microedition.lcdui.Image;

import azure.lyt.LayoutGraphics;

public abstract class Button extends ILayoutDrawable{

	public boolean isSelected = false;

	@Override
	public abstract void draw();

	public static class Str extends Button{
		private String text;

		public Str(String text){
			this.text = text;
		}

		@Override
		public void draw(){
			if (!isSelected){
				LayoutGraphics.g.drawString(text, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
			}
			else {
				LayoutGraphics.g.drawString(">" + text, x - 4, y, LayoutGraphics.ANCHOR_TOP_LEFT);
			}
		}

		public void drawSelected(){
		}
	}

	public static class Graphical extends Button{
		private Image idle;
		private Image highlighted;

		public Graphical(Image resource, Image resource_h){
			idle = resource;
			highlighted = resource_h;
		}

		@Override
		public void draw(){
			if (!isSelected){
				LayoutGraphics.g.drawImage(idle, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
			}
			else {
				LayoutGraphics.g.drawImage(highlighted, x, y, LayoutGraphics.ANCHOR_TOP_LEFT);
			}
		}
	}
}

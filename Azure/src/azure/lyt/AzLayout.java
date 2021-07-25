package azure.lyt;

import azure.common.ResourceManager;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.lcdui.Image;

import azure.lyt.elements.ILayoutDrawable;
import azure.lyt.elements.Label;
import lejos.nxt.LCD;

public abstract class AzLayout {
	private List<ILayoutDrawable> drawables = new ArrayList<>();

	public abstract String getResourceRoot();

	public void add(ILayoutDrawable d, String id) {
		d.name = id;
		drawables.add(d);
	}

	public void add(ILayoutDrawable d) {
		d.name = null;
		drawables.add(d);
	}

	public void add(ILayoutDrawable d, String id, int x, int y) {
		d.setLocation(x, y);
		d.name = id;
		drawables.add(d);
	}

	public void add(ILayoutDrawable d, int x, int y) {
		d.setLocation(x, y);
		d.name = null;
		drawables.add(d);
	}

	public void draw() {
		LCD.clear();
		for (ILayoutDrawable d : drawables) {
			if (d.isVisible()) {
				d.draw();
			}
		}
	}

	public Image getLayoutImage(String resName) {
		return ResourceManager.readImage(getResourceRoot() + resName);
	}

	public ILayoutDrawable getDrawableByName(String name) {
		for (ILayoutDrawable d : drawables) {
			if (name.equals(d.name)) {
				return d;
			}
		}
		return null;
	}

	public void setTextByLabelName(String name, String text) {
		for (ILayoutDrawable d : drawables) {
			if (d instanceof Label) {
				if (name.equals(d.name)) {
					((Label) d).setText(text);
					draw();
					break;
				}
			}
		}
	}

	public void callSequence(String name) {

	}
}

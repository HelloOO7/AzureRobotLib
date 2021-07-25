package azure.lyt.elements;

public abstract class ILayoutDrawable {

	protected byte x, y;
	private boolean isVisible = true;
	private VisibilityGroup visGroup = null;

	public String name;

	public void setVisGroup(VisibilityGroup grp){
		visGroup = grp;
	}

	public void setLocation(int x, int y){
		this.x = (byte)x;
		this.y = (byte)y;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public void setX(int x){
		this.x = (byte)x;
	}

	public void setY(int y){
		this.y = (byte)y;
	}

	public void setVisible(boolean b){
		isVisible = b;
	}

	public boolean isVisible(){
		if (visGroup != null){
			return visGroup.isVisible() & isVisible;
		}
		return isVisible;
	}

	public abstract void draw();
}

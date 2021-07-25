
package azure.lyt.elements;

public class VisibilityGroup {
	private boolean isVisible = true;
	private VisibilityGroup parent = null;

	public void setVisible(boolean b){
		isVisible = b;
	}

	public boolean isVisible(){
		if (parent != null){
			return parent.isVisible() & isVisible;
		}
		return isVisible;
	}

	public void addMembers(ILayoutDrawable... elems){
		for (ILayoutDrawable d : elems){
			d.setVisGroup(this);
		}
	}

	public void addChildGroup(VisibilityGroup grp){
		grp.parent = this;
	}
}

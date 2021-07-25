package azure.lyt;

import java.util.Stack;

public class LayoutRoot {
	public static Stack<AzLayout> layoutStack = new Stack<>();
	public static AzLayout activeLayout;

	public void pushLayout(AzLayout lyt) {
		if (activeLayout != null) {
			layoutStack.push(activeLayout);
		}
		else {
			activeLayout = lyt;
		}
	}

	public void pop(){
		activeLayout = layoutStack.pop();
	}

	public void draw(){
		if (activeLayout != null){
			activeLayout.draw();
		}
	}
}

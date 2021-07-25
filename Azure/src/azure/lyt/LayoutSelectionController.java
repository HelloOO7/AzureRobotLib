package azure.lyt;

import azure.lyt.elements.Button;
import lejos.nxt.LCD;

public class LayoutSelectionController {
	private Button[] buttons;

	private Button cache_topmost;
	private Button cache_bottommost;

	public LayoutSelectionController(Button... elems){
		buttons = elems;

		cache_topmost = buttons[0];
		cache_bottommost = buttons[0];
		for (Button b : buttons){
			if (b.getY() <= cache_topmost.getY()){
				if (b.getY() == cache_topmost.getY()){
					if (b.getX() < cache_topmost.getX()){
						cache_topmost = b;
					}
				}
				else {
					cache_topmost = b;
				}
			}

			if (b.getY() >= cache_bottommost.getY()){
				if (b.getY() == cache_bottommost.getY()){
					if (b.getX() > cache_bottommost.getX()){
						cache_bottommost = b;
					}
				}
				else {
					cache_bottommost = b;
				}
			}
		}
	}

	private Button selectedButton = null;

	public void setSelectedElement(Button button){
		for (Button b : buttons){
			if (b != button){
				b.isSelected = false;
			}
			else {
				b.isSelected = true;
				selectedButton = b;
			}
		}
	}

	public void cycleRight(){
		if (selectedButton != null){
			boolean selected = false;
			int startX = selectedButton.getX();
			OuterLoopLabel:
			for (int y = selectedButton.getY(); y < LCD.SCREEN_HEIGHT; y++){
				for (int x = startX; x < LCD.SCREEN_WIDTH; x++){
					for (Button b : buttons){
						if (b.getX() == x && b.getY() == y){
							setSelectedElement(b);
							selected = true;
							break OuterLoopLabel;
						}
					}
				}
				startX = 0;
			}
			if (!selected){
				setSelectedElement(cache_topmost);
			}
		}
		else {
			setSelectedElement(buttons[0]);
		}
	}
}

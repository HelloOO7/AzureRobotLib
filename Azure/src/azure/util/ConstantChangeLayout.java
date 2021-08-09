package azure.util;
import java.util.ArrayList;
import java.util.List;

import azure.common.EqualsMap;
import azure.lyt.AzLayout;
import azure.lyt.elements.Button;
import azure.lyt.elements.Label;
import lejos.nxt.ButtonListener;

public class ConstantChangeLayout extends AzLayout {

	private EqualsMap<String, ConstantChangeControl> controls = new EqualsMap<>();

	private List<ConstantChangeControl> controlsOrdered = new ArrayList<>();

	private int currentControlIndex = 0;

	private List<ConstantChangeCallback> callbacks = new ArrayList<>();

	public ConstantChangeLayout() {
		ButtonListener btnListener = new ButtonListener() {

			@Override
			public void buttonReleased(lejos.nxt.Button b) {

			}

			@Override
			public void buttonPressed(lejos.nxt.Button b) {
				switch (lejos.nxt.Button.readButtons()) {
					case lejos.nxt.Button.ID_ENTER:
						cyclePrev();
						break;
					case lejos.nxt.Button.ID_ESCAPE:
						cycleNext();
						break;
					case lejos.nxt.Button.ID_LEFT:
						decrementCurrent();
						break;
					case lejos.nxt.Button.ID_RIGHT:
						incrementCurrent();
						break;
				}

				for (ConstantChangeCallback cb : callbacks) {
					cb.onChange(ConstantChangeLayout.this);
				}

				draw();
			}
		};

		lejos.nxt.Button.ENTER.addButtonListener(btnListener);
		lejos.nxt.Button.ESCAPE.addButtonListener(btnListener);
		lejos.nxt.Button.LEFT.addButtonListener(btnListener);
		lejos.nxt.Button.RIGHT.addButtonListener(btnListener);
	}

	public void addCallback(ConstantChangeCallback cb) {
		if (cb != null && !callbacks.contains(cb)) {
			callbacks.add(cb);
		}
	}

	public void addControl(String tag, int step, int currentValue) {
		ConstantChangeControl ctrl = new ConstantChangeControl();
		ctrl.step = step;
		ctrl.value = currentValue;
		ctrl.button = new Button.Str(tag);
		ctrl.valueLabel = new Label(String.valueOf(currentValue));
		add(ctrl.button, 5, controls.size() * 8);
		add(ctrl.valueLabel, 84, controls.size() * 8);
		controls.put(tag, ctrl);
		controlsOrdered.add(ctrl);
	}

	public void addControl(IConstantChangeEnum enumValue) {
		ConstantChangeControl ctrl = new ConstantChangeControl();
		ctrl.step = enumValue.getStep();
		ctrl.value = enumValue.getValue();
		ctrl.button = new Button.Str(enumValue.toString());
		ctrl.valueLabel = new Label(String.valueOf(ctrl.value));
		add(ctrl.button, 5, controls.size() * 8);
		add(ctrl.valueLabel, 84, controls.size() * 8);
		controls.put(enumValue.toString(), ctrl);
		controlsOrdered.add(ctrl);
	}

	public int getControlValue(String tag) {
		return controls.get(tag).value;
	}

	public void cycleNext() {
		currentControlIndex++;
		if (currentControlIndex >= controls.size()) {
			currentControlIndex = 0;
		}
	}

	public void cyclePrev() {
		currentControlIndex--;
		if (currentControlIndex < 0) {
			currentControlIndex = controls.size() - 1;
		}
	}

	public void incrementCurrent() {
		ConstantChangeControl c = controlsOrdered.get(currentControlIndex);
		c.value += c.step;
		if (c.enumTarget != null) {
			c.enumTarget.setValue(c.value);
		}
	}

	public void decrementCurrent() {
		ConstantChangeControl c = controlsOrdered.get(currentControlIndex);
		c.value -= c.step;
		if (c.enumTarget != null) {
			c.enumTarget.setValue(c.value);
		}
	}

	@Override
	public void draw(){
		for (int i = 0; i < controlsOrdered.size(); i++) {
			ConstantChangeControl c = controlsOrdered.get(i);
			c.valueLabel.setText(String.valueOf(c.value));
			c.button.isSelected = i == currentControlIndex;
		}
		super.draw();
	}

	@Override
	public String getResourceRoot() {
		return "/invalid/";
	}

	public static class ConstantChangeControl {
		public int step;
		public int value;

		public IConstantChangeEnum enumTarget;

		public Button.Str button;
		public Label valueLabel;
	}

	public static interface ConstantChangeCallback {
		public void onChange(ConstantChangeLayout lyt);
	}
}

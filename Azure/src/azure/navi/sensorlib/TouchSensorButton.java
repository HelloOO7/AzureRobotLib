package azure.navi.sensorlib;

import lejos.nxt.TouchSensor;

public class TouchSensorButton {
	private TouchSensor ts;

	private long lastPressedMillis;
	private int lastHoldTime;

	private boolean pressed = false;
	private boolean held = false;
	private boolean released = false;

	public TouchSensorButton(TouchSensor ts) {
		this.ts = ts;
	}

	private long lastReleaseTime = -1;

	public void update() {
		pressed = false;
		released = false;
		boolean lastHeld = held;
		held = ts.isPressed();
		long time = System.currentTimeMillis();
		if (held != lastHeld) {
			released = !held;
			if (released) {
				lastReleaseTime = time;
			}
			if (lastReleaseTime == -1 || time - lastReleaseTime >= 70) {
				lastReleaseTime = -1;
				pressed = held;
			}
		}
		if (pressed) {
			lastPressedMillis = time;
		}
		if (held || released) {
			lastHoldTime = (int)(time - lastPressedMillis);
		}
	}

	public boolean isHeld() {
		return held;
	}

	public boolean isNowReleased() {
		return released;
	}

	public boolean isNowPressed() {
		return pressed;
	}

	public int getLastHoldTime() {
		return lastHoldTime;
	}
}

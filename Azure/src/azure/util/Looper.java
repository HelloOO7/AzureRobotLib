package azure.util;

public class Looper {
	private final int min;
	private final int max;

	private int nextVal = 0;

	public Looper(int min, int max) {
		this.min = min;
		this.max = max;
		nextVal = min - 1;
	}

	public void forceValue(int val) {
		nextVal = val;
	}

	public int current() {
		return nextVal;
	}

	public int next() {
		nextVal++;
		if (nextVal > max) {
			nextVal = min;
		}
		return nextVal;
	}
}
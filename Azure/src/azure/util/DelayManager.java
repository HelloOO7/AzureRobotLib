package azure.util;

public class DelayManager {
	private long lastNanotime;

	public void beginDelay(){
		lastNanotime = System.nanoTime();
	}

	public void delayFor(long nanos) {
		delayUntil(System.nanoTime() + nanos);
	}

	public void delayForIncrementalMs(long ms) {
		long nanos = ms * 1000000;
		delayUntil(lastNanotime + nanos);
		lastNanotime += nanos;
	}

	public void delayUntil(long nanos) {
		long target = nanos;
		while (System.nanoTime() < target) {
			Thread.yield();
		}
	}

	public void delayUntilModuloDiff(int mod) {
		long time = System.nanoTime() - lastNanotime;
		long target = time - (time % mod) + mod + lastNanotime;
		delayUntil(target);
	}
}

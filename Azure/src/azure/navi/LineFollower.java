package azure.navi;

import azure.navi.sensorlib.LightSensorController;
import azure.navi.sensorlib.SensorCondition;
import azure.navi.sensorlib.SensorConfig;
import lejos.nxt.Sound;

/**
 * Hyper-complicated line following class. DO NOT USE.
 */
public class LineFollower {

	public static float coef_p;
	public static float coef_i;
	public static float coef_d;

	private static FollowerClock clock = new FollowerClock();
	private static MotorPair mot;
	private static LightSensorController ls;

	public static void setCoefficients(float p, float i, float d) {
		coef_p = p;
		coef_i = i;
		coef_d = d;
	}

	public static void followSeq(SensorConfig sc, MotorPair motors, SensorCondition condition) {
		if (!sc.hasLightSensor()) {
			throw new UnsupportedOperationException("Can't follow without a LightSensor.");
		}
		ls = sc.getLightSensor();
		mot = motors;
		clock.reset();

		mot.setDefaultSpeed(450f);
		mot.setToDefaultSpeed();
		mot.startTheEngines();

		lastTime = System.currentTimeMillis();
		while (condition.pass()) {
			doFollowSubroutine2();
			Sys.delay(25);
			Thread.yield();
		}

		mot.haltTheEngines();
	}

	static long lastTime;
	static float panic_coef;
	static float lastSignum = 0;
	static float acc = 1;

	private static void doFollowSubroutine2() {
		float val = ls.getUnitValue();
		float nowSignum = Math.signum(val);
		if (nowSignum != lastSignum) {
			acc = 1;
		} else {
			acc += val * 0.2;
		}
		if (nowSignum == -1) {
			val *= 2;
		}
		lastSignum = nowSignum;
		panic_coef = val;
		panic_coef = Math.min(1, Math.max(-1, panic_coef));
		float finc = panic_coef * 100 * acc;
		finc = Math.min(400, Math.max(-400, finc));
		mot.setEngineSpeedSeparate(mot.getDefaultSpeed() - finc, mot.getDefaultSpeed() + finc);
	}

	private static void doFollowSubroutine() {
		int now = clock.getTick();
		if (now - clock.getLastSubTime() < 10) {
			Sys.delay(12 - (now - clock.getLastSubTime()));
			now = clock.getTick();
		}
		float dt = 0.001f * (now - clock.getLastSubTime());
		float e = ls.getUnitValue();
		/* 1=bila -1=cerna, cil je 0 */
		if (dt > 0.1f) {
			Sound.beep(); // chyba -- musite to volat aspon 10* za sekundu
		} else {
			clock.increaseAcc(coef_i * e * dt);
			float diff = coef_d * (e - clock.getLastLightValue()) / dt;
			float a = e * coef_p + clock.getAcc() + diff;
			if (a > 1) {
				a = 1;
			}
			if (a < -1) {
				a = -1;
			}
			float ls = (float) Math.toDegrees(mot.getDefaultSpeed() * (1 - a * mot.getDirMult()));
			float rs = (float) Math.toDegrees(mot.getDefaultSpeed() * (1 + a * mot.getDirMult()));
			float dms = mot.getDefaultSpeed();
			if (ls > dms) {
				ls = dms;
			}
			if (rs > dms) {
				rs = dms;
			}
			if (ls <= 5) {
				ls = 5;
			}
			if (rs <= 5) {
				rs = 5;
			}
			mot.setEngineSpeedSeparate(ls, rs);
		}
		clock.setLastSubTime();
		clock.setLastLightValue(e);
		Thread.yield();
	}

	private static class FollowerClock {

		private long start;
		private float lastLightValue = 0;
		private int lastSubroutineTime = 0;
		private float acc = 0;

		public FollowerClock() {
			reset();
		}

		public void reset() {
			start = System.currentTimeMillis();
			setLastSubTime();
			acc = 0;
			lastLightValue = 0;
		}

		public int getTick() {
			return (int) (System.currentTimeMillis() - start);
		}

		public void setLastSubTime() {
			lastSubroutineTime = getTick();
		}

		public int getLastSubTime() {
			return lastSubroutineTime;
		}

		public void setLastLightValue(float v) {
			lastLightValue = v;
		}

		public float getLastLightValue() {
			return lastLightValue;
		}

		public void increaseAcc(float value) {
			acc = Math.max(-1, Math.min(1, value));
		}

		public float getAcc() {
			return acc;
		}
	}
}

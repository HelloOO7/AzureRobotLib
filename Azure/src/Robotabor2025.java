/**
 * EasyRobotLibrary.
 *
 * @author Dr. David (TM), Tomáš, Čeněk.
 * @version 2026.1.compat
 */
public class Robotabor2025 extends Robotabor {
	
	public static void init_buggy(float prumer_kola, float rozchod_kol) {
		initBuggy(prumer_kola, rozchod_kol);
	}
	
	public static void init_buggy(float prumer_kola, float rozchod_kol, NXTRegMotor levy_motor,
			NXTRegMotor pravy_motor) {
		initBuggy(prumer_kola, rozchod_kol, levy_motor, pravy_motor);
	}

	public static int getb() {
		return getButton();
	}
	
	public static int readb() {
		return readButton();
	}
	
	public static int ms_time() {
		return msTime();
	}
	
	public static void ms_sleep(int milliseconds) {
		msSleep(milliseconds);
	}
	
	public boolean is_going() {
		return isGoing();
	}
	
	public float current_distance() {
		return getCurrentDistance();
	}
	
	public void calibrate_light(RobotaborLightSensor ls, int uhel) {
		calibrateBuggy(ls, uhel);
	}
	
	public void find_track(float searchSpeed) {
		findTrack(searchSpeed);
	}
	
	public static void start_following(float p, float i, float d, float bsat, float wsat, float sdecay) {
		startFollowing(p, i, d, bsat, wsat, sdecay);
	}
	
	public static void stop_following() {
		stopFollowing();
	}
	
	public static void recalibrate_on_white() {
		recalibrateOnWhite();
	}
}

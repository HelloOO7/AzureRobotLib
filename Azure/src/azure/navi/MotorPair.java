package azure.navi;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;

public class MotorPair {

	protected NXTRegulatedMotor engineL;
	protected NXTRegulatedMotor engineR;

	public MechanicsType m = MechanicsType.TANDEM;
	public MotorDirConfig md = MotorDirConfig.FORWARD;
	private EngineDir engineDir = EngineDir.FWD;

	private float distanceMul = 1f;

	public MotorPair(MotorPort left, MotorPort right, MechanicsType m) {
		this(left, right, m, MotorDirConfig.FORWARD);
	}

	/**
	 * Instantiates a MotorPair with a given configuration.
	 * @param left Port of the "left" motor.
	 * @param right Port of the "right" motor.
	 * @param m MechanicsType relation between the two motors.
	 */
	public MotorPair(MotorPort left, MotorPort right, MechanicsType m, MotorDirConfig dir) {
		engineL = new NXTRegulatedMotor(left);
		engineR = new NXTRegulatedMotor(right);
		this.md = dir;
		this.m = m;
		setDirFwd();
	}

	private float dist;
	private float radius;

	/**
	 * Sets the engine speed in millimeters per second.
	 * @param mmps Speed in mm/s.
	 */
	public void setEngineSpeedMmps(float mmps) {
		float degps = (float) Math.toDegrees(mmps / radius);
		engineL.setSpeed(degps);
		engineR.setSpeed(degps);
	}
	
	public void setEngineSpeedSeparateMmps(float mmpsL, float mmpsR) {
		engineL.setSpeed((float) Math.toDegrees(mmpsL / radius));
		engineR.setSpeed((float) Math.toDegrees(mmpsR / radius));
	}

	/**
	 * Sets constants required for accurate turning emulation.
	 * @param distance Distance between the motor wheels.
	 * @param diameter Diameter of both wheels.
	 */
	public void setTurnConstants(float distance, float diameter) {
		dist = distance / 2f;
		radius = diameter / 2f;
	}

	/**
	 * Turns the machine by a given amount.
	 * @param deg Turn amount in degrees.
	 */
	public void turn(float deg) {
		resetMotorState();
		float alfa = deg * dist
			/ radius;
		engineR.rotate(Math.round(getRValue(alfa)), true);
		engineL.rotate(Math.round(getLValue(-alfa)), false);
	}

	/**
	 * Travels an absolute distance.
	 * @param millimeters Distance in milimeters.
	 */
	public void go(float millimeters) {
		resetMotorState();
		float alfa = mmToDeg(millimeters) * distanceMul;
		System.out.println(alfa + " radius " + radius);
		engineR.rotate(Math.round(getRValue(alfa)), true);
		engineL.rotate(Math.round(getLValue(alfa)), false);
	}

	public void setDistanceMul(float mul) {
		distanceMul = mul;
	}

	public float degToMm(float deg) {
		return (float)(Math.toRadians(deg) * radius);
	}

	public float mmToDeg(float mm) {
		return (float) Math.toDegrees(mm / radius);
	}

	private float getLValue(float val) {
		return md == MotorDirConfig.INVERSE ? -val : val;
	}

	private float getRValue(float val) {
		return (m == MechanicsType.COUNTER ^ md == MotorDirConfig.INVERSE) ? -val : val;
	}

	private static final float DEFAULT_SPEED = 360f;
	private float defaultSpeed = DEFAULT_SPEED;
	private int defaultStopPeriod = 3500;

	/**
	 * Resets the default speed value to the library default of 360 degrees per second.
	 */
	public void resetDefaultSpeed() {
		defaultSpeed = DEFAULT_SPEED;
	}

	/**
	 * Sets the motor speed to the 'default speed' value.
	 */
	public void setToDefaultSpeed() {
		setEngineSpeed(defaultSpeed);
	}

	/**
	 * Sets the default speed value to an user-provided constant.
	 * The method has no effect until the engines are told to use the default speed.
	 *
	 * @param value Speed in degrees per second.
	 */
	public void setDefaultSpeed(float value) {
		defaultSpeed = value;
	}

	/**
	 * Gets the default speed value.
	 *
	 * @return Default speed.
	 */
	public float getDefaultSpeed() {
		return defaultSpeed;
	}

	public int getAcceleration() {
		return engineL.getAcceleration();
	}
	
	public int getSpeed() {
		return engineL.getSpeed();
	}

	/**
	 * Sets the engine speed for both engines.
	 *
	 * @param value Speed in degrees per second.
	 */
	public void setEngineSpeed(float value) {
		engineL.setSpeed(value);
		engineR.setSpeed(value);
	}

	public void setEngineAcceleration(int a) {
		engineL.setAcceleration(a);
		engineR.setAcceleration(a);
	}

	/**
	 * Sets the engine speed separately for each motor.
	 *
	 * @param l Left engine speed in degrees per second.
	 * @param r Right engine speed in degrees per second.
	 */
	public void setEngineSpeedSeparate(float l, float r) {
		engineL.setSpeed(l);
		engineR.setSpeed(r);
	}

	/**
	 * Toggles the motor direction. Requires calling startTheEngines to commit the state.
	 */
	public void toggleDirection() {
		if (engineDir == EngineDir.FWD) {
			engineDir = EngineDir.BWD;
		} else {
			engineDir = EngineDir.FWD;
		}
	}

	public int getTachoCount() {
		int left = engineL.getTachoCount();
		int right = engineR.getTachoCount();
		if (md == MotorDirConfig.INVERSE) {
			left = -left;
			right = -right;
		}
		if (m == MechanicsType.COUNTER) {
			right = -right;
		}
		return (left + right) / 2;
	}

	/**
	 * Changes the motor direction to forwards. Requires calling startTheEngines to commit the state.
	 */
	public void setDirFwd() {
		engineDir = md == MotorDirConfig.INVERSE ? EngineDir.BWD : EngineDir.FWD;
	}

	/**
	 * Changes the motor direction to backwards. Requires calling startTheEngines to commit the state.
	 */
	public void setDirBwd() {
		engineDir = md == MotorDirConfig.INVERSE ? EngineDir.FWD : EngineDir.BWD;
	}

	/**
	 * Resets the tacho of both engines.
	 */
	public void resetMotorState() {
		engineL.resetTachoCount();
		engineR.resetTachoCount();
	}

	/**
	 * Starts both of the engines in the set motion direction.
	 */
	public void startTheEngines() {
		motorGoFwdBwd(engineL, engineDir, false);
		motorGoFwdBwd(engineR, engineDir, m == MechanicsType.COUNTER);
	}

	/**
	 * Internal function for setting motor direction based on mechanics configuration.
	 */
	private static void motorGoFwdBwd(NXTRegulatedMotor motor, EngineDir dir, boolean invert) {
		boolean fwd = dir == EngineDir.FWD ^ invert;
		if (fwd) {
			motor.forward();
		} else {
			motor.backward();
		}
	}

	/**
	 * Stops the engines using the default stop period.
	 */
	public void stopTheEngines() {
		stopTheEngines(defaultStopPeriod);
	}

	/**
	 * Gradually slows down the motors till a complete halt in a given time period.
	 *
	 * @param period How long to slow down for.
	 */
	public void stopTheEngines(int period) {
		float speed = engineL.getSpeed();
		float decrease = speed / (period / 10f);
		for (float i = speed; i > 0; i -= decrease) {
			setEngineSpeed((int) i);
			Sys.delay(10);
		}
		haltTheEngines();
	}

	/**
	 * Immediately ceases engine motion.
	 */
	public void haltTheEngines() {
		int acce = engineL.getAcceleration();
		setEngineAcceleration(6000);
		engineL.stop(true);
		engineR.stop();
		setEngineAcceleration(acce);
		resetMotorState();
	}

	/**
	 * Gets a single-component normalized direction vector of the engine direction.
	 *
	 * @return 1 if the motors are running forwards, -1 if backwards.
	 */
	public float getDirMult() {
		return engineDir == EngineDir.FWD ? 1 : -1;
	}

	/**
	 * Type of the motor mechanics.
	 */
	public static enum MechanicsType {
		/**
		 * Both of the engines run in the same direction.
		 */
		TANDEM,
		/**
		 * One engine runs in reverse direction than the other.
		 */
		COUNTER
	}

	public static enum MotorDirConfig {
		FORWARD,
		INVERSE
	}

	/**
	 * Direction in which the engines are running.
	 */
	private static enum EngineDir {
		/**
		 * Forward motion.
		 */
		FWD,
		/**
		 * Backward motion.
		 */
		BWD
	}
}

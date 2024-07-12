package azure.navi.sensorlib;

import azure.lyt.AzLayout;
import azure.navi.Sys;
import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LightSensorController {
	private LightSensor sen;

	private short primaryColor;
	private short secondaryColor;

	private LightValueSet priColorOnoff;
	private LightValueSet secColorOnoff;

	/**
	 * Creates a controller by a sensor port.
	 * @param port The sensor port.
	 */
	protected LightSensorController(SensorPort port) {
		sen = new LightSensor(port);
	}

	/**
	 * Wraps a controller around a pre-constructed sensor object.
	 * @param sen The light sensor.
	 */
	protected LightSensorController(LightSensor sen){
		this.sen = sen;
	}

	/**
	 * Calibrates the sensors using the given layout system.
	 * @param lyt Compatible calibration layout.
	 */
	public void doCalibrationSequence(AzLayout lyt) {
		lyt.setTextByLabelName("clyt_desc", getPrimaryColorName());
		Button.waitForAnyPress();
		primaryColor = (short) getImmediateValue();
		priColorOnoff = getOnOffValues();
		lyt.setTextByLabelName("clyt_desc", getSecondaryColorName());
		Button.waitForAnyPress();
		secondaryColor = (short) getImmediateValue();
		secColorOnoff = getOnOffValues();
		lyt.callSequence("clyt_finish");
		Button.waitForAnyPress();
		lyt.callSequence("clyt_hide");
	}
	
	public void calibrate(int pri, int sec) {
		this.primaryColor = (short)pri;
		this.secondaryColor = (short)sec;
	}

	/**
	 * Gets the sensor value with floodlight on.
	 * @return Normalized light value.
	 */
	public int getImmediateValue() {
		if (!sen.isFloodlightOn()) {
			sen.setFloodlight(true);
			Sys.delay(20);
		}
		return sen.getNormalizedLightValue();
	}

	/**
	 * Checks if the value is closest to the primary controller color.
	 * @return
	 */
	public boolean getValueIsPrimary() {
		int value = getImmediateValue();
		return Math.abs(primaryColor - value) < Math.abs(secondaryColor - value);
	}

	/**
	 * Checks if the value is closest to the secondary controller color.
	 * @return
	 */
	public boolean getValueIsSecondary() {
		return !getValueIsPrimary();
	}

	/**
	 * Checks if the value matches the primary color within a threshold.
	 * @param threshold
	 * @return True if the absolute difference between the immediate sensor value and the primary color is lower than the threshold.
	 */
	public boolean getValueIsPrimary(int threshold){
		return Math.abs(primaryColor - getImmediateValue()) <= threshold;
	}

	/**
	 * Checks if the value matches the secondary color within a threshold.
	 * @param threshold
	 * @return True if the absolute difference between the immediate sensor value and the secondary color is lower than the threshold.
	 */
	public boolean getValueIsSecondary(int threshold){
		return Math.abs(secondaryColor - getImmediateValue()) <= threshold;
	}

	/**
	 * Gets a normalized weight value that represents the distance between the primary, current and secondary color.
	 * @return -1 if the color is equal to the primary color, 1 if it is equal to the secondary color, or something in between.
	 */
	public float getUnitValue() {
		int c = getImmediateValue();
		return Math.min(1, Math.max(-1, ((c - primaryColor) / (float)(secondaryColor - primaryColor) - 0.5f) * 2));
	}

	public float getUnitValueNoClamp() {
		int c = getImmediateValue();
		return ((c - primaryColor) / (float)(secondaryColor - primaryColor) - 0.5f) * 2;
	}

	public float getUnitValueOnoff() {
		LightValueSet onoff = getOnOffValues();
		float weightOn = (onoff.valueOn - priColorOnoff.valueOn) / (float)(secColorOnoff.valueOn - priColorOnoff.valueOn);
		float weightOff = (onoff.valueOff - priColorOnoff.valueOff) / (float)(secColorOnoff.valueOff - priColorOnoff.valueOff);
		System.out.println(weightOn + "/" + weightOff);
		return (weightOff + weightOn) - 1f;
	}

	/**
	 * Gets the values read from the light sensor when the floodlight is off and on.
	 * @return
	 */
	public LightValueSet getOnOffValues() {
		sen.setFloodlight(false);
		Sys.delay(20);
		int off = sen.getNormalizedLightValue();
		sen.setFloodlight(true);
		Sys.delay(20);
		int on = sen.getNormalizedLightValue();
		return new LightValueSet(off, on);
	}

	/**
	 * Blocks until the primary color is reached.
	 */
	public void waitForPrimaryColor(){
		while (!getValueIsPrimary()){
			Thread.yield();
		}
	}

	/**
	 * Blocks until the secondary color is reached.
	 */
	public void waitForSecondaryColor(){
		while (!getValueIsSecondary()){
			Thread.yield();
		}
	}

	public int getPrimaryColor(){
		return primaryColor;
	}

	public int getSecondaryColor(){
		return secondaryColor;
	}

	public String getPrimaryColorName() {
		return "PRIMARY";
	}

	public String getSecondaryColorName() {
		return "SECONDARY";
	}

	public static class BlackWhiteSensorController extends LightSensorController {
		public BlackWhiteSensorController(SensorPort port) {
			super(port);
		}

		public BlackWhiteSensorController(LightSensor s) {
			super(s);
		}

		@Override
		public String getPrimaryColorName() {
			return "BLACK";
		}

		@Override
		public String getSecondaryColorName() {
			return "WHITE";
		}

		public boolean isBlack(){
			return getValueIsPrimary();
		}

		public boolean isWhite(){
			return getValueIsSecondary();
		}
	}

	public static class RedBlueSensorController extends LightSensorController {
		public RedBlueSensorController(SensorPort port) {
			super(port);
		}

		@Override
		public String getPrimaryColorName() {
			return "RED";
		}

		@Override
		public String getSecondaryColorName() {
			return "BLUE";
		}
	}

	public static class LightValueSet {
		public int valueOff;
		public int valueOn;

		public LightValueSet(int off, int on) {
			valueOff = off;
			valueOn = on;
		}

		/**
		 * Lerps this light value set with another. Result is stored into `this`.
		 * @param src
		 */
		public void interpolateWith(LightValueSet src) {
			valueOff = (src.valueOff + valueOff) / 2;
			valueOn = (src.valueOn + valueOn) / 2;
		}

		public int getLightOffset() {
			return Math.abs(valueOff - valueOn);
		}

		public int compareOffsets(LightValueSet comp) {
			return Math.abs(comp.getLightOffset() - getLightOffset());
		}
	}
}

package azure.navi.sensorlib;

import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;

public class SensorConfig {
	private UltrasonicSensor us = null;
	private LightSensorController ls = null;
	private TouchSensor ts0 = null;
	private TouchSensor ts1 = null;

	public void bindSensor(SensorType sen, int port){
		SensorPort sp = getSensorByUniqueID(port);
		switch (sen){
			case TOUCH_PRI:
				ts0 = new TouchSensor(sp);
				break;
			case TOUCH_SEC:
				ts1 = new TouchSensor(sp);
				break;
			case ULTRA_SONIC:
				us = new UltrasonicSensor(sp);
				break;
			default:
				throw new IllegalArgumentException(sen + " < Unhandled sensor type.");
		}
	}

	public void bindLightSensor(LightSensorType type, int port){
		SensorPort sp = getSensorByUniqueID(port);
		switch (type){
			case BW:
				ls = new LightSensorController.BlackWhiteSensorController(sp);
				break;
			case RB:
				ls = new LightSensorController.RedBlueSensorController(sp);
				break;
		}
	}

	private static SensorPort getSensorByUniqueID(int id){
		switch (id){
			case 1:
				return SensorPort.S1;
			case 2:
				return SensorPort.S2;
			case 3:
				return SensorPort.S3;
			case 4:
				return SensorPort.S4;
		}
		throw new IllegalArgumentException(id + " < Invalid port");
	}

	public UltrasonicSensor getUltraSonicSensor(){
		return us;
	}

	public LightSensorController getLightSensor(){
		return ls;
	}

	public TouchSensor getTouchSensor(){
		return getPrimaryTouchSensor();
	}

	public TouchSensor getPrimaryTouchSensor(){
		return ts0;
	}

	public TouchSensor getSecondaryTouchSensor(){
		return ts1;
	}

	public boolean hasLightSensor(){
		return ls != null;
	}

	public static enum SensorType{
		ULTRA_SONIC,
		LIGHT,
		TOUCH_PRI,
		TOUCH_SEC
	}

	public static enum LightSensorType{
		RB,
		BW
	}
}

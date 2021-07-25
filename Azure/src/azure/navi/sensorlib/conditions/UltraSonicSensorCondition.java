package azure.navi.sensorlib.conditions;

import azure.navi.sensorlib.SensorCondition;
import lejos.nxt.UltrasonicSensor;

public class UltraSonicSensorCondition implements SensorCondition{
	private UltrasonicSensor sen;
	private CommonMathOp op;
	private short param;

	public UltraSonicSensorCondition(UltrasonicSensor sensor, CommonMathOp op, int parameter) {
		sen = sensor;
		this.op = op;
		param = (short)parameter;
	}

	@Override
	public boolean pass() {
		int value = sen.getDistance();
		switch(op){
			case EQUAL:
				return param == value;
			case GEQUAL:
				return value >= param;
			case GREATER:
				return value > param;
			case LEQUAL:
				return value <= param;
			case LESS:
				return value < param;
			case NOTEQUAL:
				return param != value;
			default:
				return false;
		}
	}
}

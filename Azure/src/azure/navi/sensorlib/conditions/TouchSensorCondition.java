package azure.navi.sensorlib.conditions;

import azure.navi.sensorlib.SensorCondition;
import lejos.nxt.TouchSensor;

public class TouchSensorCondition implements SensorCondition{

	private TouchSensor sen;
	private CommonMathOp op;

	public TouchSensorCondition(TouchSensor sen, CommonMathOp op) {
		if (op != CommonMathOp.EQUAL && op != CommonMathOp.NOTEQUAL){
			throw new IllegalArgumentException("The TouchSensor only supports EQUAL and NOTEQUAL ops.");
		}
		this.sen = sen;
		this.op = op;
	}

	public TouchSensorCondition(TouchSensor sen) {
		this(sen, CommonMathOp.EQUAL);
	}

	@Override
	public boolean pass() {
		if (op == CommonMathOp.NOTEQUAL){
			return !sen.isPressed();
		}
		else {
			return sen.isPressed();
		}
	}
}

package azure.navi.sensorlib;

public interface SensorCondition {

	public boolean pass();

	public static enum CommonMathOp {
		EQUAL,
		NOTEQUAL,
		GREATER,
		GEQUAL,
		LESS,
		LEQUAL
	}
}

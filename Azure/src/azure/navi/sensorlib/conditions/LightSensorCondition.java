package azure.navi.sensorlib.conditions;

import azure.navi.sensorlib.LightSensorController;
import azure.navi.sensorlib.SensorCondition;

public class LightSensorCondition implements SensorCondition{

	private LightSensorController ls;
	private CommonMathOp op;
	private ConstantColor constantColor;
	private int customColor;

	private LightSensorCondition(LightSensorController ls, CommonMathOp op, ConstantColor cc, int color){
		this.ls = ls;
		this.op = op;
		constantColor = cc;
		customColor = color;
	}

	public LightSensorCondition(LightSensorController ls, ConstantColor cc) {
		this(ls, CommonMathOp.EQUAL, cc, -1);
	}

	public LightSensorCondition(LightSensorController ls, CommonMathOp op, int color) {
		this(ls, op, ConstantColor.NONE, color);
	}

	public LightSensorCondition(LightSensorController ls, CommonMathOp op, ConstantColor cc) {
		this(ls, op, cc, -1);
	}

	@Override
	public boolean pass() {
		if (op == CommonMathOp.EQUAL && constantColor != ConstantColor.NONE){
			if (constantColor == ConstantColor.PRIMARY_COLOR){
				return ls.getValueIsPrimary();
			} else {
				return ls.getValueIsSecondary();
			}
		}
		int col = -1;
		switch (constantColor){
			case NONE:
				col = customColor;
				break;
			case PRIMARY_COLOR:
				col = ls.getPrimaryColor();
				break;
			case SECONDARY_COLOR:
				col = ls.getSecondaryColor();
				break;
		}
		int value = ls.getImmediateValue();
		switch (op){
			case EQUAL:
				return value == col;
			case GEQUAL:
				return value >= col;
			case GREATER:
				return value > col;
			case LEQUAL:
				return value <= col;
			case LESS:
				return value < col;
			case NOTEQUAL:
				return value != col;
		}
		return false;
	}

	public static enum ConstantColor{
		PRIMARY_COLOR,
		SECONDARY_COLOR,
		NONE
	}
}

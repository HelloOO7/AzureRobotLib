package azure.bt.packets;

import azure.bt.AzPacket;
import azure.common.AzInputStream;

public class DefaultPacket extends AzPacket{

	public DefaultPacket(String value){
		super(1);
		bufferFormats[0] = DataType.STRING;
		values[0] = value;
	}

	public DefaultPacket(AzInputStream in) {
		super(in);
	}

	public String getMessage(){
		return (String)values[0];
	}
}

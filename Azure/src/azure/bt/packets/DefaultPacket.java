package azure.bt.packets;

import azure.bt.AzPacket;
import azure.common.AzInputStream;

public class DefaultPacket extends AzPacket {

	public DefaultPacket(String value) {
		bufferFormats = new DataType[] { DataType.STRING };
		values = new Object[] { value };
	}

	public DefaultPacket(AzInputStream in) {
		super(in);
	}

	public String getMessage() {
		return (String) values[0];
	}
}

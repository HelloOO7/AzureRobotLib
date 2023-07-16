package azure.bt;

import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;

public class AzureRS485Connection extends AzureNXTConnectionBase {

	public AzureRS485Connection(String target) {
		super(target);
	}

	public AzureRS485Connection() {
		this(null);
	}

	@Override
	protected NXTConnection openConnection() {
		if (target == null) {
			return RS485.waitForConnection(0, NXTConnection.RAW);
		} else {
			return RS485.connect(target, NXTConnection.RAW);
		}
	}
}

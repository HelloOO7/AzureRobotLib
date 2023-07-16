package azure.bt;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class AzureBTConnection extends AzureNXTConnectionBase {

	public AzureBTConnection(String target) {
		super(target);
	}

	public AzureBTConnection() {
		this(null);
	}

	public NXTConnection openConnection() {
		if (target == null) {
			return Bluetooth.waitForConnection(0, NXTConnection.RAW);
		} else {
			return Bluetooth.connect(target, NXTConnection.RAW, null);
		}
	}
}

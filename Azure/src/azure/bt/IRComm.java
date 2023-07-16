package azure.bt;

import azure.navi.Sys;
import azure.util.DelayManager;
import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;

public class IRComm {

	public static final int IRCOMM_LIGHT_T = 10;
	public static final int IRCOMM_PAIRDELAY = 50;
	public static final int IRCOMM_PAIRDELAY_TOLERANCE = 8;
	public static final int IRCOMM_LIGHTON_THRESHOLD = 400;

	public static LightSensor ls = new LightSensor(SensorPort.S1);

	private static DelayManager delay = new DelayManager();

	public static void main(String[] args) {
		delay.beginDelay();

		ls.setFloodlight(false);
		boolean isMaster = false;
		if (Bluetooth.getFriendlyName().equals("NXT53")) {
			isMaster = true;
		}
		System.out.println(isMaster ? "MASTER" : "SLAVE");

		Button.waitForAnyPress();

		if (!isMaster) {
			System.out.println("Waiting for connection");
			waitForConnection();
			System.out.println("Connected. Waiting for stream.");
			waitForStreamBegin();
			int o1 = readOctet();
			int o2 = readOctet();
			int o3 = readOctet();
			int o4 = readOctet();
			int o5 = readOctet();
			System.out.println(o1 + "/" + o2 + "/" + o3 + "/" + o4 + "/" + o5);
		} else {
			connect();
			notifyStreamBegin();
			writeOctet(1);
			writeOctet('t');
			writeOctet('e');
			writeOctet('s');
			writeOctet('t');
		}

		System.out.println("fini");
		Button.waitForAnyPress();
	}

	public static String readString() {
		int len = readOctet();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append((char)readOctet());
		}
		return sb.toString();
	}

	public static void writeString(String str) {
		writeOctet(str.length());
		for (int i = 0; i < str.length(); i++) {
			writeOctet(str.charAt(i));
		}
	}

	public static void waitForConnection() {
		ls.setFloodlight(false);
		while (!checkIncomingDataOn()) {
			Thread.yield();
		}
	}

	public static void connect() {
		delay.beginDelay();
		ls.setFloodlight(true);
		delay.delayForIncrementalMs(IRCOMM_LIGHT_T);
	}

	public static void notifyStreamBegin() {
		delay.beginDelay();
		Sound.setVolume(100);
		ls.setFloodlight(false);
		delay.delayForIncrementalMs(IRCOMM_PAIRDELAY);
		ls.setFloodlight(true);
		delay.delayForIncrementalMs(IRCOMM_PAIRDELAY);
		ls.setFloodlight(false);
		delay.delayForIncrementalMs(IRCOMM_PAIRDELAY);
		ls.setFloodlight(true);
		delay.beginDelay();
	}

	public static void waitForStreamBegin() {
		Sound.setVolume(100);
		WaitLoop: while (true) {
			delay.beginDelay();
			while (checkIncomingDataOn()) {
				delay.delayForIncrementalMs(1);
			}
			//floodlight off for 50 secs
			for (int i = 0; i < IRCOMM_PAIRDELAY; i++) {
				if (i > IRCOMM_PAIRDELAY_TOLERANCE) {
					if (checkIncomingDataOn()) {
						if (Math.abs(i - IRCOMM_PAIRDELAY) < IRCOMM_PAIRDELAY_TOLERANCE) {
							break;
						} else {
							if (checkIncomingDataOn()) {
								System.out.println("Pairdelay ON at " + i + "ms! Stream verification FAILED.");
								continue WaitLoop;
							}
						}
					}
				}
				delay.delayForIncrementalMs(1);
			}
			//floodlight on for 50 secs
			for (int i = 0; i < IRCOMM_PAIRDELAY; i++) {
				if (i > IRCOMM_PAIRDELAY_TOLERANCE) {
					if (!checkIncomingDataOn()) {
						if (Math.abs(i - IRCOMM_PAIRDELAY) < IRCOMM_PAIRDELAY_TOLERANCE) {
							break;
						} else {
							if (!checkIncomingDataOn()) {
								System.out.println("Pairdelay OFF at " + i + "ms! Stream verification FAILED.");
								continue WaitLoop;
							}
						}
					}
				}
				delay.delayForIncrementalMs(1);
			}
			while (checkIncomingDataOn()) {
				Thread.yield();
			}
			//turned off, wait till floodlight on
			while (!checkIncomingDataOn()) {
				Thread.yield();
			}
			delay.beginDelay();
			return;
		}
	}

	public static void writeOctet(int value) {
		for (int i = 0; i < 8; i++) {
			boolean flash = (value & 1) == 1;
			value >>= 1;
			ls.setFloodlight(flash);

			delay.delayUntilModuloDiff(IRCOMM_LIGHT_T * 1000000);
		}
	}

	private static int debug_lastValue = 0;

	public static boolean checkIncomingDataOn() {
		debug_lastValue = ls.readNormalizedValue();
		return debug_lastValue > IRCOMM_LIGHTON_THRESHOLD;
	}

	private static boolean[] octet_buf = new boolean[40];
	private static boolean[] octet_buf_raw = new boolean[40];

	public static int readOctet() {
		int octet = 0;

		boolean bit;
		boolean modded = false;
		for (int i = 0; i < 40; i++) {
			bit = checkIncomingDataOn();
			octet_buf_raw[i] = bit;

			if (!modded && i > 0 && octet_buf[i - 1] != bit) {
				int mod = i % 5;
				if (mod != 0) {
					int j = i;
					i += 5 - mod;
					for (int k = j; k < i; k++) {
						octet_buf[k] = !bit;
					}
					modded = true;
				}
			}

			octet_buf[i] = bit;
			delay.delayUntilModuloDiff(IRCOMM_LIGHT_T * 200000);
		}

		for (int i = 0; i < octet_buf.length; i++) {
			System.out.print(octet_buf[i] ? "1" : "0");
		}
		for (int i = 0; i < octet_buf.length; i += 5) {
			octet >>= 1;
			int nOn = 0;
			int nOff = 0;
			for (int j = i; j < i + 5; j++) {
				if (octet_buf[j]) {
					nOn++;
				}
				else {
					nOff++;
				}
			}
			if (nOn > nOff) {
				octet |= 128;
			}
		}
		return octet;
	}
}

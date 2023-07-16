package azure.bt;
import java.io.IOException;

import azure.common.Arc.ArcFileInfo;
import azure.common.ResourceManager;
import lejos.nxt.Button;

/**
 * Tests for various Azure subsystems.
 */
public class TestingRuntime {
	public static void main(String[] args){
		archiveTest();
	}

	/**
	 * Prints all contents of the default resource archive.
	 */
	public static void archiveTest(){
		ResourceManager.init();
		/*for (ArcFileInfo info : ResourceManager.lyArc.files){
			System.out.println(info.getFullResourceName());
			Button.waitForAnyPress();
		}*/
		Button.waitForAnyPress();
	}

	/**
	 * Server-side method for Bluetooth comm test.
	 */
	public static void sendTest(){
		AzureConnection con = new AzureBTConnection("BLUE");
		try {
			System.out.println("Connection established, sending");
			con.send("test");
			System.out.println("Sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Button.waitForAnyPress();
	}

	/**
	 * Client-side method for Bluetooth comm test.
	 */
	public static void recvTest(){
		AzureConnection con = new AzureBTConnection();
		System.out.println("Connection established");
		con.waitForMessage();
		String read = con.nextMessage();
		System.out.println(read);
		Button.waitForAnyPress();
	}
}

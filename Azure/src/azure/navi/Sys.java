package azure.navi;

public class Sys {

	/*
	Exception-safe method to sleep a thread.
	*/
	public static void delay(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

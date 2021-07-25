package azure.navi;

public class Sys {
	
	/*
	Exception-safe method to sleep a thread.
	*/
	public static void delay(int ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

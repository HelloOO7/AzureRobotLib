package azure.seq;

import java.io.File;
import azure.common.ResourceManager;

public class Instrument {
	private static final String AZURE_SDAT_ROOT = "/sdat/sample/";

	private byte[] impact;
	private byte[] falloff;

	public Instrument(String name){
		//String impactName = AZURE_SDAT_ROOT + name + "_Impact.wav";
		//String falloffName = AZURE_SDAT_ROOT + name + "_Falloff.wav";

		//impact = ResourceManager.lyArc.getFile(impactName);
		//falloff = ResourceManager.lyArc.getFile(falloffName);
	}

    static native void playSample(int page, int offset, int len, int freq, int vol);

    int piano = new File("Piano_Impact.wav").getPage();

	public void playNote(int freq, int length){
		playSample(piano, 44, 1177, (int)((freq / 440f) * 8000), 100);
	}

	public static byte[] getSampleData(String instrumentName){
		return ResourceManager.lyArc.getFile(AZURE_SDAT_ROOT + instrumentName);
	}
}

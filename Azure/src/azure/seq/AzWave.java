package azure.seq;

import java.io.IOException;

import azure.common.AzInputStream;
import azure.common.ResourceManager.Resource;
import azure.util.NativeFlash;

public class AzWave {

	public static final String SIGNATURE = "AZWAVSND";
	public static final int HEADER_LENGTH = SIGNATURE.length() + 2 + 4;

	private final int page;
	private final int offset;

	private int sampleRate;
	private int sampleCount;

	public AzWave(Resource res) {
		page = res.getPage(HEADER_LENGTH);
		offset = res.getOffsetInPage(HEADER_LENGTH);

		try {
			AzInputStream dis = new AzInputStream(res.openStream());

			if (!dis.getMagic(SIGNATURE)) {
				dis.close();
				throw new RuntimeException("Wave is not an AZWAVSND format file!");
			}

			sampleRate = dis.readUnsignedShort();
			sampleCount = dis.readInt();

			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getSampleRate(){
		return sampleRate;
	}

    public static native void playFreq(int aFrequency, int aDuration, int aVolume);
    public static native void playSample(int page, int offset, int len, int freq, int vol);
    public static native int playQueuedSample(byte[] data, int offset, int len, int freq, int vol);

    public int getLenMillis() {
    	return getLenMillis(sampleCount);
    }

    public int getLenSamples() {
    	return sampleCount;
    }

    public int getLenMillis(int lenSamples) {
    	return (lenSamples * 1000) / sampleRate;
    }

    public int getLenSamples(int millis) {
    	return (millis * sampleRate) / 1000;
    }

    public int playRange(int first, int count, int freq, int volume) {
    	return playQueuedSample(null, NativeFlash.getFlashRawAddress(page, offset + first), count, freq, volume);
    }

	public void play(float speed, int volume, int maxLenMillis) {
		int maxLen = sampleCount;
		if (maxLenMillis != -1) {
			maxLen = getLenSamples(maxLenMillis);
			if (maxLen > sampleCount) {
				maxLen = sampleCount;
			}
		}
		playSample(page, offset, maxLen, (int)(sampleRate * speed), volume);
	}
}

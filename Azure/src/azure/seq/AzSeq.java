package azure.seq;

import java.io.IOException;
import java.io.InputStream;
import azure.common.AzInputStream;
import lejos.nxt.Sound;

public class AzSeq {

	public static final String AZURE_SEQ_HEADER = "AZURESEQ";

	/**
	 * Plays an AzureStudio sound sequence.
	 * @param in An input stream to read the sequence from.
	 */
	public static void playSequence(InputStream in){
		try {
			AzInputStream dis = new AzInputStream(in);
			dis.getMagic(AZURE_SEQ_HEADER);
			dis.readString();	//Instrument, unused
			int tickLength = dis.readUnsignedShort();
			int cmdCount = dis.readInt();

			long start = System.currentTimeMillis();

			for (int i = 0; i < cmdCount; i++){
				Command cmd = new Command(dis);
				long startTgt = start + getMsForTicks(cmd.tick);
				long current = System.currentTimeMillis();
				int lendif = 0;
				if (current < startTgt){
					Thread.sleep(startTgt - current);
				}
				else {
					lendif = (int)(startTgt - current);
				}

				Sound.playNote(Sound.PIANO, getFreqForKey(cmd.tone), getMsForTicks(cmd.length) + lendif);
			}
			dis.close();

			Thread.sleep(start + getMsForTicks(tickLength) - System.currentTimeMillis());
		} catch (InterruptedException | IOException e){
			e.printStackTrace();
		}
	}

	private static final float TICK_REAL_SIZE = 2.0f;

	private static int getMsForTicks(int ticks){
		return (int)(ticks * TICK_REAL_SIZE);
	}

	public static final float[] tones = {16.35f, 17.32f, 18.35f, 19.45f, 20.6f, 21.83f, 23.12f, 24.5f, 25.96f, 27.50f, 29.14f, 30.87f};

	private static int getFreqForKey(int key){
        int octave = (key / 12) -1;
        int note = key % 12;
        return (int)(tones[note] * Math.pow(2, octave));
	}

	private static class Command{
		public int tick;
		public short tone;
		public int length;

		public Command(AzInputStream dis) throws IOException{
			tick = dis.readUnsignedShort();
			tone = (short)dis.read();
			length = dis.readUnsignedShort();
		}
	}
}

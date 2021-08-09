package azure.seq;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import azure.common.AzInputStream;
import azure.common.ResourceManager;
import lejos.nxt.Sound;

public class AzSeq {

	public static final String AZURE_SEQ_HEADER = "AZURESEQ";

	public static final int AZSEQCMD_TYPE_CHGINST = 1;

	/**
	 * Plays an AzureStudio sound sequence.
	 * @param in An input stream to read the sequence from.
	 */
	public static void playSequence(InputStream in){
		try {
			AzInputStream dis = new AzInputStream(in);
			dis.getMagic(AZURE_SEQ_HEADER);

			SequencingContext ctx = new SequencingContext(dis);

			int cmdCount = dis.readInt();

			long start = System.currentTimeMillis();

			for (int i = 0; i < cmdCount; i++){
				CommandExecutor exec = null;

				int key = dis.read();
				int tick = dis.readUnsignedShort();
				if (key == 0xFF) {
					int type = dis.read();

					switch (type) {
						case AZSEQCMD_TYPE_CHGINST:
							exec = CHGINST_EXECUTOR_INSTANCE;
							break;
					}
				}
				else {
					exec = PLAY_TONE_EXECUTOR_INSTANCE;
				}

				if (exec == null) {
					continue;
				}

				exec.playTimestamp = (long)(start + ctx.tickOffset + tick * ctx.tickScale);
				exec.key = key;

				long current = System.currentTimeMillis();
				if (current < exec.playTimestamp){
					Thread.sleep(exec.playTimestamp - current);
				}

				exec.execute(dis, ctx);
			}
			dis.close();

			//System.out.println("AZSEQ finished, sleep for " + (start + ctx.tickLength - System.currentTimeMillis()));
			Thread.sleep(start + ctx.tickLength - System.currentTimeMillis());
		} catch (InterruptedException | IOException e){
			e.printStackTrace();
		}
	}

	public static final float[] tones = {16.35f, 17.32f, 18.35f, 19.45f, 20.6f, 21.83f, 23.12f, 24.5f, 25.96f, 27.50f, 29.14f, 30.87f};

	private static int getFreqForKey(int key){
        int octave = (key / 12) - 1;
        int note = key % 12;
        return (int)(tones[note] * Math.pow(2, octave));
	}

	private static final PlayToneCommandExecutor PLAY_TONE_EXECUTOR_INSTANCE = new PlayToneCommandExecutor();
	private static final ChangeInstrumentCommandExecutor CHGINST_EXECUTOR_INSTANCE = new ChangeInstrumentCommandExecutor();

	private static class SequencingContext {
		public long tickLength;

		public float tickOffset;
		public float tickScale;
		public float lengthOffset;
		public float lengthScale;

		public List<String> instrumentNames = new ArrayList<>();

		public AzWave currentInstrument = null;

		public SequencingContext(AzInputStream in) throws IOException {
			int instCount = in.readUnsignedByte();

			for (int i = 0; i < instCount; i++) {
				instrumentNames.add(in.readString());
			}

			tickLength = in.readLong();
			tickOffset = in.readFloat();
			tickScale = in.readFloat();
			lengthOffset = in.readFloat();
			lengthScale = in.readFloat();
		}

		public void setInstrument(int index) {
			currentInstrument = new AzWave(ResourceManager.lyArc, ResourceManager.lyArc.getFileDescriptor("/sdat/sample/" + instrumentNames.get(index) + ".wav"));
		}
	}

	private static abstract class CommandExecutor {
		public int key;
		public long playTimestamp;

		public abstract void execute(AzInputStream dis, SequencingContext ctx) throws IOException;
	}

	private static class PlayToneCommandExecutor extends CommandExecutor {

		@Override
		public void execute(AzInputStream dis, SequencingContext ctx) throws IOException {
			long current = System.currentTimeMillis();
			int lendif = 0;
			if (current > playTimestamp) {
				lendif = (int)(playTimestamp - current);
			}

			int playLen = (int)(ctx.lengthOffset + dis.read() * ctx.lengthScale + lendif);

			if (ctx.currentInstrument == null) {
				Sound.playNote(Sound.PIANO, getFreqForKey(key), playLen);
			}
			else {
				ctx.currentInstrument.play(getFreqForKey(key) / 440f, Sound.getVolume(), playLen);
			}
		}
	}

	private static class ChangeInstrumentCommandExecutor extends CommandExecutor {

		@Override
		public void execute(AzInputStream dis, SequencingContext ctx) throws IOException {
			int instIdx = dis.read();
			ctx.setInstrument(instIdx);
		}

	}
}

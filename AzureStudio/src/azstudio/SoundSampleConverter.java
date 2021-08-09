package azstudio;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SoundSampleConverter {

	private static final int RIFF_RIFF_SIG = 0x52494646;
	private static final int RIFF_WAVE_SIG = 0x57415645;
	private static final int RIFF_FMT_SIG = 0x666d7420;
	private static final short RIFF_FMT_PCM = 0x0100;
	private static final short RIFF_FMT_1CHAN = 0x0100;
	private static final short RIFF_FMT_8BITS = 0x0800;
	private static final int RIFF_DATA_SIG = 0x64617461;

	public static byte[] convertSoundSample(File source) {
		try {
			AzWave wave = new AzWave(source);

			return wave.serialize();
		} catch (Exception e) {
			System.err.println("Could not convert WAV file " + source + ". [" + e.getMessage() + "]");
			return null;
		}
	}

	public static class AzWave {
		public static final String SIGNATURE = "AZWAVSND";

		public int sampleRate;

		public byte[] samples;

		@SuppressWarnings("resource")
		public AzWave(File file) {
			DataInputStream in;
			try {
				in = new DataInputStream(new FileInputStream(file));

				if (in.readInt() != RIFF_RIFF_SIG) {
					throw new RuntimeException("Source is not a valid waveform!");
				}
				in.readInt();

				if (in.readInt() != RIFF_WAVE_SIG) {
					throw new RuntimeException("Wave signature not present!");
				}
				if (in.readInt() != RIFF_FMT_SIG)
					throw new RuntimeException("Format block expected!");

				// Now check that the format is PCM, Mono 8 bits. Note that
				// these
				// values are stored little endian.
				int fmtSize = Integer.reverseBytes(in.readInt());
				if (in.readShort() != RIFF_FMT_PCM) {
					throw new RuntimeException("WAV needs to be PCM format!");
				}
				if (in.readShort() != RIFF_FMT_1CHAN) {
					throw new RuntimeException("WAV can only have one channel!");
				}
				sampleRate = Integer.reverseBytes(in.readInt());
				in.readInt();
				in.readShort();
				if (in.readShort() != RIFF_FMT_8BITS) {
					throw new RuntimeException("WAV is not 8-bit!");
				}
				in.skip(fmtSize - 16);

				for (;;) {
					int chunkSignature = in.readInt();
					int dataLen = Integer.reverseBytes(in.readInt());

					if (chunkSignature == RIFF_DATA_SIG) {
						samples = new byte[dataLen];
						in.read(samples);
						break;
					} else {
						in.skipBytes(dataLen);
					}
				}

				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public byte[] serialize() {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				DataOutputStream dos = new DataOutputStream(out);

				dos.write(SIGNATURE.getBytes(StandardCharsets.US_ASCII));
				dos.writeShort(sampleRate);
				dos.writeInt(samples.length);

				dos.write(samples);

				dos.close();
				return out.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}

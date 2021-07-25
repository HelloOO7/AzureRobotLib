package azstudio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class SoundSeqConverter {
	public static void main(String[] args) {
		try {
			byte[][] channels = convertToMultiChannel(new File("azure_resources/sdat/Super Mario 64 Slider.mid"));
			int h = 0;
			for (byte[] data : channels) {
				Files.write(Paths.get("azure_resources/sdat/Slider" + h + ".azseq"), data, StandardOpenOption.WRITE,
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				h++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] convert(File midiFile) {
		try {
			Sequence seq = MidiSystem.getSequence(midiFile);
			return new AzSeq(seq).serialize();
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[][] convertToMultiChannel(File midiFile) {
		System.out.println("converting " + midiFile.getName());
		try {
			Sequence seq = MidiSystem.getSequence(midiFile);
			byte[][] r = new byte[seq.getTracks().length][];
			for (int i = 0; i < seq.getTracks().length; i++) {
				r[i] = new AzSeq(seq, i).serialize();
			}
			return r;
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class AzSeq {
		public static final String AZURE_SEQ_HEADER = "AZURESEQ";

		public int tickLength;
		public List<AzSeqCmd> commands = new ArrayList<>();

		public AzSeq(Sequence mid) {
			for (Track t : mid.getTracks()) {
				commands.addAll(convertTrackToCommandList(t, mid.getResolution()));
			}

			postProcessCommandList(commands);

			tickLength = (int) (mid.getTickLength() / retrieveTickScale(mid.getTracks()[0], mid.getResolution()));
		}

		public AzSeq(Sequence mid, int track) {
			float tickScale = retrieveTickScale(mid.getTracks()[0], mid.getResolution());

			commands = convertTrackToCommandList(mid.getTracks()[track], mid.getResolution(), tickScale);

			tickLength = (int) (mid.getTickLength() / tickScale);
		}

		public float retrieveTickScale(Track t, float midiResolution) {
			for (int i = 0; i < t.size(); i++) {
				MidiEvent evt = t.get(i);
				MidiMessage msg = evt.getMessage();
				int cmd = msg.getMessage()[0] & 0xFF;
				if (cmd == 0xFF) {
					switch (msg.getMessage()[1]) {
					case 0x51:
						int ts = ((msg.getMessage()[3] & 0xFF) << 16) | ((msg.getMessage()[4] & 0xFF) << 8)
								| (msg.getMessage()[5] & 0xFF);
						return (float) (ts / 50000f * (midiResolution / 1000f));
					}
				}
			}
			return midiResolution / 100f;
		}

		public static List<AzSeqCmd> convertTrackToCommandList(Track t, float midiResolution) {
			return convertTrackToCommandList(t, midiResolution, -1);
		}

		public static List<AzSeqCmd> convertTrackToCommandList(Track t, float midiResolution, float tickScaleOverride) {
			float tickScale = midiResolution / 100f;
			boolean tsSet = false;
			if (tickScaleOverride != -1){
				tickScale = tickScaleOverride;
				tsSet = true;
			}
			List<AzSeqCmd> commands = new ArrayList<>();
			Map<Integer, Integer> notesDown = new HashMap<>(); // Note, tick
			for (int i = 0; i < t.size(); i++) {
				MidiEvent evt = t.get(i);
				int tick = (int) (evt.getTick());
				MidiMessage msg = evt.getMessage();
				int cmd = msg.getMessage()[0] & 0xFF;
				if (cmd >= 0x90 && cmd <= 0x9F) {
					int key = msg.getMessage()[1] & 0xFF;
					if (msg.getMessage()[2] == 0 && notesDown.containsKey(key)) {
						int originNoteTick = notesDown.get(key);
						commands.add(new AzSeqCmd((int) (originNoteTick / tickScale), key,
								(int) ((tick - originNoteTick) / tickScale)));
						notesDown.remove(key);
					} else {
						notesDown.put(key, tick);
					}
				} else if (cmd == 0xFF) {
					switch (msg.getMessage()[1]) {
					case 0x51:
						if (tsSet){
							break;
						}
						int ts = ((msg.getMessage()[3] & 0xFF) << 16) | ((msg.getMessage()[4] & 0xFF) << 8)
								| (msg.getMessage()[5] & 0xFF);
						tickScale = (float) (ts / 50000f * (midiResolution / 1000f));
						tsSet = true;
						break;
					}
				} else if (cmd >= 0x80 && cmd <= 0x8F) {
					int key = msg.getMessage()[1] & 0xFF;
					if (!notesDown.containsKey(key)) {
						continue;
					}
					int originNoteTick = notesDown.get(key);
					commands.add(new AzSeqCmd((int) (originNoteTick / tickScale), key,
							(int) ((tick - originNoteTick) / tickScale)));
					notesDown.remove(key);
				}
			}

			postProcessCommandList(commands);

			return commands;
		}

		public static void postProcessCommandList(List<AzSeqCmd> commands) {
			commands.sort(new Comparator<AzSeqCmd>() {
				@Override
				public int compare(AzSeqCmd i1, AzSeqCmd i2) {
					return i1.tick - i2.tick;
				}
			});
			for (int i = 0; i < commands.size(); i++) {
				int tick = commands.get(i).tick;
				for (int j = 0; j < i; j++) {
					AzSeqCmd alter = commands.get(j);
					alter.length = (int) (Math.min(alter.length, tick - alter.tick));
				}
			}
		}

		public byte[] serialize() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.write(AZURE_SEQ_HEADER.getBytes("ASCII"));
			StringUtils.writeString(dos, "Piano");
			dos.writeShort(tickLength);
			dos.writeInt(commands.size());
			for (AzSeqCmd cmd : commands) {
				cmd.serialize(dos);
			}
			out.close();
			return out.toByteArray();
		}

		public static class AzSeqCmd {
			private int tick;
			private byte key;
			private int length;

			public AzSeqCmd(int tick, int key, int length) {
				this.tick = tick;
				this.key = (byte) key;
				this.length = (short) length;
			}

			public void serialize(DataOutputStream out) throws IOException {
				out.writeShort(tick);
				out.write(key);
				out.writeShort(length);
			}
		}
	}
}

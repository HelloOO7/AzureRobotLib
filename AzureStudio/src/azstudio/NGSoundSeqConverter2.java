package azstudio;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import azstudio.NGSoundSeqConverter.AzSeq.AzSeqChangeInstrumentCmd;
import azstudio.NGSoundSeqConverter.AzSeq.AzSeqCmd;
import azstudio.NGSoundSeqConverter.AzSeq.AzSeqCmdPlayTone;
import azstudio.NGSoundSeqConverter.AzSeq.AzSeqMetaCmd;

/**
 * Azure Sequencer 2.1.
 * Forked from InoSeq, version 2.0, which was
 * forked from Azure Sequencer, version 2.0.
 */
public class NGSoundSeqConverter2 {

	public static final int COMMAND_SET_TEMPO = 0x51;
	
	private static final Map<Integer, String> GENERALUSER_TO_AZSEQ_MAP = new HashMap<>();

	static {
		GENERALUSER_TO_AZSEQ_MAP.put(0, "GrandPno");
		GENERALUSER_TO_AZSEQ_MAP.put(57, "Trombone");
		GENERALUSER_TO_AZSEQ_MAP.put(61, "BrassSect");
		/*GENERALUSER_TO_AZSEQ_MAP.put(64, "Sax");
		GENERALUSER_TO_AZSEQ_MAP.put(65, "Sax");
		GENERALUSER_TO_AZSEQ_MAP.put(66, "Sax");*/
	}

	public static byte[] convertDownmixed(File midiFile) {
		try {
			Sequence seq = MidiSystem.getSequence(midiFile);
			return new AzSeq(seq, -1, -1).serialize();
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static byte[][] convertToMultiChannel(File midiFile) {
		System.out.println("Converting " + midiFile.getName());
		try {
			Sequence seq = MidiSystem.getSequence(midiFile);
			List<byte[]> output = new ArrayList<>();
			for (int trackNo = 0; trackNo < seq.getTracks().length; trackNo++) {
				for (int channelNo = 0; channelNo < 10; channelNo++) {
					AzSeq azs = new AzSeq(seq, trackNo, channelNo);
					if (!azs.commands.isEmpty()) {
						output.add(azs.serialize());
					}
				}
			}
			return output.toArray(new byte[output.size()][]);
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class AzSeq {
		
		public static final String AZURE_SEQ_HEADER = "AZURESEQ";
		public static final int AZSEQCMD_TYPE_CHGINST = 1;
		
		private final float defaultTickScale;

		private final long microsecLength;

		private float currentTempoScale;
		private float currentTickOffset;
		private long currentTickOffsetMid;

		public List<AzSeqCmd> commands = new ArrayList<>();
		public List<String> instrumentNames = new ArrayList<>();

		private float convertTick(long tick) {
			return (tick - currentTickOffsetMid) * currentTempoScale + currentTickOffset;
		}

		public AzSeq(Sequence seq, int trackNo, int channelNo) {
			int origChannelNo = channelNo;
			if (channelNo == -1) {
				channelNo = 0;
			}
			Track track;
			if (trackNo == -1) {
				track = seq.createTrack();
				for (Track t2 : seq.getTracks()) {
					if (t2 != track) {
						for (int i = 0; i < t2.size(); i++) {
							MidiEvent evt = t2.get(i);

							byte[] mesg = evt.getMessage().getMessage();

							int cmd = mesg[0] & 0xFF;

							if ((cmd & 0xF0) == 0x80 || ((cmd & 0xF0) == 0x90)) {
								if (origChannelNo == -1 || origChannelNo == (cmd & 0x0F)) {
							    	try {
										MidiEvent newEvent = new MidiEvent(new ShortMessage(cmd & 0xF0, channelNo, mesg[1], 0), evt.getTick());
										track.add(newEvent);
									} catch (InvalidMidiDataException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
						    }
						}
					}
				}
			}
			else {
				track = seq.getTracks()[trackNo];
			}
			microsecLength = seq.getMicrosecondLength();

			Track tempoTrack = seq.getTracks()[0];

			if (track != tempoTrack) {
				for (int i = 0; i < tempoTrack.size(); i++) {
					MidiEvent evt = tempoTrack.get(i);

					byte[] mesg = evt.getMessage().getMessage();

					int cmd = mesg[0] & 0xFF;

					if (cmd == 0xFF && mesg[1] == COMMAND_SET_TEMPO) {
						track.add(evt);
					}
				}
			}

			float divisionType = seq.getDivisionType();
			int resolution = seq.getResolution();

			if (divisionType == Sequence.PPQ) {
				defaultTickScale = resolution;
				// milliseconds per tick
			} else {
				// resolution = ticks per frame
				float ticksps = divisionType * resolution;
				defaultTickScale = (1f / ticksps) * 1000f;
				// milliseconds per tick
			}

			currentTempoScale = 500000f / defaultTickScale / 1000f * 0.693333f;

			Map<Integer, Float> notesNowPlaying = new HashMap<>();

			boolean hasNote = false;

			System.out.println("Begin track " + trackNo + " channel " + channelNo);
			for (int i = 0; i < track.size(); i++) {
				MidiEvent evt = track.get(i);

				byte[] mesg = evt.getMessage().getMessage();

				int cmd = mesg[0] & 0xFF;

				switch (cmd) {
				case 0xFF:
					switch (mesg[1]) {
					case COMMAND_SET_TEMPO:
						int tempo = getInt24(mesg, 3);
						System.out.println("OLD TEMPO: " + currentTempoScale + " resol " + resolution);
						currentTickOffset = convertTick(evt.getTick());
						currentTickOffsetMid = evt.getTick();
						currentTempoScale = tempo / defaultTickScale / 1000f;
						System.out.println("BPM " + tempo + " scale " + currentTempoScale);
						break;
					default:
						//System.out.println("AZSEQ incompatible command " + mesg[1] + Arrays.toString(mesg));
						break;
					}
					break;
				}

				if (cmd == 0x80 + channelNo) {
					// NOTE OFF
					int key = mesg[1] & 0xFF;
					if (notesNowPlaying.containsKey(key)) {
						float startTime = notesNowPlaying.get(key);
						float nowTime = convertTick(evt.getTick());

						commands.add(new AzSeqCmdPlayTone(startTime, key, nowTime - startTime));

						notesNowPlaying.remove(key);
					}
				} else if (cmd == 0x90 + channelNo) {
					hasNote = true;
					// NOTE ON
					int key = mesg[1] & 0xFF;
					float nowTime = convertTick(evt.getTick());
					//System.out.println("NoteON " + nowTime + " acttick " + evt.getTick() + " key " + key);
					if (mesg[2] == 0 && notesNowPlaying.containsKey(key)) {
						// Turn note off
						float noteStartTime = notesNowPlaying.get(key);
						commands.add(new AzSeqCmdPlayTone(noteStartTime, key, nowTime - noteStartTime));
						notesNowPlaying.remove(key);
					} else {
						// Register not for turn-on
						notesNowPlaying.put(key, nowTime);
					}
				} else if (cmd == 0xC0 + channelNo) {
					//change instrument
					String instName = GENERALUSER_TO_AZSEQ_MAP.get((mesg[1] & 0xFF));
					int index;
					if (instName == null) {
						index = 0;
						System.err.println("Unrecognized instrument: " + ((mesg[1] & 0xFF)));
					}
					else {
						index = instrumentNames.indexOf(instName);
						if (index == -1) {
							index = instrumentNames.size();
							instrumentNames.add(instName);
						}
					}

					commands.add(new AzSeqChangeInstrumentCmd(convertTick(evt.getTick()), index));
				}
			}

			if (!hasNote) {
				commands.clear();
				return;
			}

			commands.sort(new Comparator<AzSeqCmd>() {

				@Override
				public int compare(AzSeqCmd o1, AzSeqCmd o2) {
					return (int)(o1.tick - o2.tick);
				}
			});

			AzSeqCmdPlayTone last = null;
			int lastIndex = -1;

			for (int i = 0; i < commands.size(); i++) {
				AzSeqCmd cmd = commands.get(i);
				if (cmd instanceof AzSeqCmdPlayTone) {
					AzSeqCmdPlayTone tone = (AzSeqCmdPlayTone) cmd;

					if (last != null) {
						if (tone.tick - last.tick < 0.01f) {
							if (last.key >= tone.key) {
								commands.remove(i);
								i--;
								continue;
							}
							else {
								commands.remove(lastIndex);
								i--;
							}
						}
					}

					last = tone;
					lastIndex = i;
				}
			}

			for (int i = 0; i < commands.size() - 1; i++) {
				AzSeqCmd thisCmd = commands.get(i);
				AzSeqCmd nextCmd = commands.get(i + 1);
				if (thisCmd.tick >= nextCmd.tick) {
					thisCmd.tick = nextCmd.tick - 1f;
				}
			}
		}

		public static int getInt24(byte[] arr, int off) {
			return ((arr[off] & 0xFF) << 16) | ((arr[off + 1] & 0xFF) << 8) | (arr[off + 2] & 0xFF);
		}

		public byte[] serialize() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			dos.write(AZURE_SEQ_HEADER.getBytes("ASCII"));

			dos.write(instrumentNames.size());
			for (String instName : instrumentNames) {
				StringUtils.writeString(dos, instName);
			}

			float tickMin = Float.MAX_VALUE;
			float tickMax = -Float.MAX_VALUE;
			float lengthMin = Float.MAX_VALUE;
			float lengthMax = -Float.MAX_VALUE;

			for (AzSeqCmd cmd : commands) {
				if (cmd.tick < tickMin) {
					tickMin = cmd.tick;
				}
				if (cmd.tick > tickMax) {
					tickMax = cmd.tick;
				}
				if (cmd instanceof AzSeqCmdPlayTone) {
					AzSeqCmdPlayTone qcmd = (AzSeqCmdPlayTone)cmd;
					if (qcmd.length < lengthMin) {
						lengthMin = qcmd.length;
					}
					if (qcmd.length > lengthMax) {
						lengthMax = qcmd.length;
					}
				}
			}

			float tickScale = (tickMax - tickMin) / 65535f;
			float lengthScale = (lengthMax - lengthMin) / 255f;
			float invTickScale = 1f / tickScale;
			float invLengthScale = 1f / lengthScale;

			dos.writeLong(microsecLength / 1000);

			dos.writeFloat(tickMin);
			dos.writeFloat(tickScale);
			dos.writeFloat(lengthMin);
			dos.writeFloat(lengthScale);

			dos.writeInt(commands.size());
			for (AzSeqCmd cmd : commands) {
				cmd.serialize(dos, tickMin, invTickScale, lengthMin, invLengthScale);
			}
			out.close();
			return out.toByteArray();
		}
		
		public static abstract class AzSeqCmd {

			public float tick;

			public abstract void serialize(DataOutputStream out, float tickOffset, float tickScale, float lengthOffset,
					float lengthScale) throws IOException;
		}

		public static class AzSeqCmdPlayTone extends AzSeqCmd {
			private byte key;

			private float length;

			public AzSeqCmdPlayTone(float tick, int key, float length) {
				this.tick = tick;
				this.key = (byte) key;
				this.length = (short) length;
			}

			public void serialize(DataOutputStream out, float tickOffset, float tickScale, float lengthOffset,
					float lengthScale) throws IOException {
				out.write(key);

				out.writeShort((short) ((tick - tickOffset) * tickScale));
				out.writeByte((byte) ((length - lengthOffset) * lengthScale));
			}
		}

		public static abstract class AzSeqMetaCmd extends AzSeqCmd {
			public void writeMetacmdBase(DataOutputStream out, int metaCmdId, float tickOffset, float tickScale,
					float lengthOffset, float lengthScale) throws IOException {
				out.write(0xFF);
				out.writeShort((short) ((tick - tickOffset) * tickScale));
				out.write(metaCmdId);
			}
		}

		public static class AzSeqChangeInstrumentCmd extends AzSeqMetaCmd {

			private int instrumentNo;

			public AzSeqChangeInstrumentCmd(float tick, int instrumentLookupNo) {
				this.tick = tick;
				this.instrumentNo = instrumentLookupNo;
			}

			@Override
			public void serialize(DataOutputStream out, float tickOffset, float tickScale, float lengthOffset,
					float lengthScale) throws IOException {
				writeMetacmdBase(out, AZSEQCMD_TYPE_CHGINST, tickOffset, tickScale, lengthOffset, lengthScale);
				out.write(instrumentNo);
			}
		}
	}
}

package azure.seq;

import java.io.InputStream;

import azure.common.ResourceManager;

public class SequencePlayer {
	public static final String SDAT_ROOT = "/sdat/";

	/**
	 * Prepares a thread for playing a sound sequence.
	 * @param seqName Name of the sequence in the default SDAT structure.
	 * @param channel The channel to be played.
	 * @param loop Whether the playback should loop after it finishes.
	 * @return The thread, ready to be started.
	 */
	public static Thread createSequenceThread(final String seqName, final int channel, final boolean loop) {
		Thread sound = new Thread() {
			@Override
			public void run() {
				AzSeq.playSequence(getAzSeqForNameAndChannel(seqName, channel));
				while (!interrupted() && loop) {
					AzSeq.playSequence(getAzSeqForNameAndChannel(seqName, channel));
				}
			}
		};
		return sound;
	}

	/**
	 * Creates an input stream of a sequence file in the default SDAT resource.
	 * @param name Name of the sequence.
	 * @param channel Channel of the sequence.
	 * @return The input stream.
	 */
	public static InputStream getAzSeqForNameAndChannel(String name, int channel) {
		String fullName = SDAT_ROOT + name + ".mid/channel" + channel + ".azseq";
		return ResourceManager.lyArc.getFileAsStream(fullName);
	}

	/**
	 * Initializes a sequence playback and plays it in the background.
	 * @param seqName Name of the sequence.
	 * @param channel Channel of the sequence.
	 */
	public static void playSequence(String seqName, int channel) {
		playSequence(seqName, channel, false);
	}

	/**
	 * Initializes a sequence playback thread and starts it.
	 * @param seqName Name of the sequence to play.
	 * @param channel Channel of the sequence.
	 * @param join True if the method should block until the playback finishes.
	 */
	public static void playSequence(String seqName, int channel, boolean join) {
		Thread player = createSequenceThread(seqName, channel, false);
		player.start();
		if (join) {
			try {
				player.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void playSequenceAndLoop(String seqName, int channel, boolean join) {
		Thread player = createSequenceThread(seqName, channel, true);
		player.start();
		if (join) {
			try {
				player.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

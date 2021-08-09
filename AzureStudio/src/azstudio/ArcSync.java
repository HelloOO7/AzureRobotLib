package azstudio;

import java.io.File;

import nxjupload.NXJFileSystem;

public class ArcSync {
	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[] { ArcConverter.AZURE_ARC_DEFAULT_RESOURCE + ArcConverter.AZURE_ARC_EXTENSION };
		}

		if (args.length > 0) {
			String path = args[0];

			File f = new File(path);

			if (f.exists() && !f.isDirectory()) {
				sendAzarcToNxt(f);
			} else {
				System.err.println("Input file is invalid.");
			}
		}
	}

	public static void sendAzarcToNxt(File arcFile) {
		NXJFileSystem fs = new NXJFileSystem();
		if (fs.makeConnection()) {
			fs.sendFile(arcFile);
		} else {
			System.err.println("Could not connect to an NXT. Please upload the archive manually.");
		}
	}
}

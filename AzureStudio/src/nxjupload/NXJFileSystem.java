package nxjupload;

import java.io.File;
import java.io.IOException;

import lejos.nxt.remote.FileInfo;
import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTConnector;

public class NXJFileSystem {

	private NXTConnector connector = new NXTConnector();

	private NXTCommand command;

	public boolean makeConnection() {
		boolean r = connector.connectTo();
		if (r) {
			command = new NXTCommand(connector.getNXTComm());
		}
		else {
			command = null;
		}
		return r;
	}

	public void sendFile(File f) {
		checkEnsureIsConnected();

		try {
			String nxtFileName = f.getName();

			if (nxtFileName.length() > 20) {
				int extIdx = nxtFileName.indexOf('.');
				if (extIdx != -1) {
					int extLen = nxtFileName.length() - extIdx;
					if (extLen < 19) {
						int nameLen = 20 - extLen;
						nxtFileName = nxtFileName.substring(0, nameLen) + nxtFileName.substring(extIdx, extIdx + extLen);
					}
					else {
						nxtFileName = nxtFileName.substring(0, 20);
					}
				}
				else {
					nxtFileName = nxtFileName.substring(0, 20);
				}
			}
			if (!nxtFileName.equals(f.getName())) {
				System.err.println("File name " + f.getName() + " over 20 characters, limiting to " + nxtFileName);
			}

			//WORKAROUND: Overwriting without deleting won't update remaining flash capacity
			FileInfo fi;
			try {
				//findFirst is broken
				fi = command.openRead(nxtFileName);
			} catch (Exception ex) {
				fi = null;
			}

			if (fi != null) {
				System.out.println("File " + fi.fileName + " already exists, deleting...");
				command.delete(fi.fileName);
				command.closeFile(fi.fileHandle);
			}

			System.out.println("Uploading file " + nxtFileName);
			command.uploadFile(f, nxtFileName);
			System.out.println("Uploaded.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkEnsureIsConnected(){
		if (command == null) {
			throw new RuntimeException("Not connected.");
		}
	}
}

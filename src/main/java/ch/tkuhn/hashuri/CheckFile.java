package ch.tkuhn.hashuri;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckFile {

	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		String hash = HashUriUtils.getHashUriDataPart(fileName);
		if (hash == null) {
			System.out.println("ERROR: No hash in file name");
			System.exit(1);
		}
		InputStream in;
		try {
			in = new URL(fileName).openConnection().getInputStream();
		} catch (MalformedURLException ex) {
			in = new FileInputStream(fileName);
		}
		
		String algorithmID = hash.substring(0, 2);
		HashUriModule module = ModuleDirectory.getModule(algorithmID);
		if (module.isCorrectHash(in, hash)) {
			System.out.println("Correct hash: " + hash);
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

}

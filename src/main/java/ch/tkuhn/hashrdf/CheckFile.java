package ch.tkuhn.hashrdf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckFile {

	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		String hash = FileUtils.getHashPart(fileName);
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
		
		RDFFileContent content = FileUtils.load(in);
		Hasher hasher = new Hasher(hash);
		String h = hasher.makeHash(content.getStatements());
		if (hash.equals(h)) {
			System.out.println("Correct hash: " + h);
		} else {
			System.out.println("*** INCORRECT HASH ***: " + h);
		}
	}

}

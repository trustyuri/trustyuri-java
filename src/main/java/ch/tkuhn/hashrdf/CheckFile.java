package ch.tkuhn.hashrdf;

import java.io.File;

public class CheckFile {

	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		String hash = FileUtils.getHashPart(fileName);
		if (hash == null) {
			System.out.println("ERROR: No hash in file name");
			System.exit(1);
		}
		File inputFile = new File(fileName);
		
		RDFFileContent content = FileUtils.loadFile(inputFile);
		Hasher hasher = new Hasher(hash);
		String h = hasher.makeHash(content.getStatements());
		if (hash.equals(h)) {
			System.out.println("Correct hash: " + h);
		} else {
			System.out.println("*** INCORRECT HASH ***: " + h);
		}
	}

}

package ch.tkuhn.hashrdf;

import java.io.File;

public class CheckFile {

	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		if (!fileName.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{43}")) {
			System.out.println("ERROR: No hash in file name");
			System.exit(1);
		}
		File inputFile = new File(fileName);
		String hash = fileName.replaceFirst("(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{43})", "$2");
		
		RDFGraphs graphs = FileUtils.loadFile(inputFile);
		Hasher hasher = new Hasher(hash);
		String h = hasher.makeHash(graphs.getStatements());
		if (hash.equals(h)) {
			System.out.println("Correct hash: " + h);
		} else {
			System.out.println("*** INCORRECT HASH ***: " + h);
		}
	}

}

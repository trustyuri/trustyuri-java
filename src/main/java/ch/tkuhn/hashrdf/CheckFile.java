package ch.tkuhn.hashrdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.openrdf.rio.trig.TriGParser;

public class CheckFile {

	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		if (!fileName.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{43}")) {
			throw new RuntimeException("No hash in file name");
		}
		File inputFile = new File(fileName);
		String hash = fileName.replaceFirst("(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{43})", "$2");
		
		InputStream in = new FileInputStream(inputFile);
		TriGParser p = new TriGParser();
		RDFGraphs graphs = new RDFGraphs();
		p.setRDFHandler(graphs);
		p.parse(in, "");
		in.close();
		Hasher hasher = new Hasher(hash);
		String h = hasher.makeHash(graphs.getStatements());
		if (hash.equals(h)) {
			System.out.println("Correct hash: " + h);
		} else {
			System.out.println("*** INCORRECT HASH ***: " + h);
		}
	}

}

package ch.tkuhn.hashuri;

import java.io.BufferedReader;
import java.io.FileReader;

import ch.tkuhn.hashuri.file.ProcessFile;
import ch.tkuhn.hashuri.rdf.CheckNanopubViaSparql;
import ch.tkuhn.hashuri.rdf.TransformLargeRdf;
import ch.tkuhn.hashuri.rdf.TransformNanopub;
import ch.tkuhn.hashuri.rdf.TransformRdf;

public class RunBatch {
	
	public static void main(String[] args) throws Exception {
		String batchFile = args[0];

		BufferedReader reader = new BufferedReader(new FileReader(batchFile));
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.charAt(0) == '#') continue;
			System.err.println("COMMAND: " + line);
			String cmd = line.replaceFirst("^([^ ]+) .*$", "$1");
			String[] cmdArgs = line.substring(line.indexOf(' ')+1).split("\\s+");
			System.gc();
			long ms = System.nanoTime();
			if (cmd.equals("CheckFile")) {
				CheckFile.main(cmdArgs);
			} else if (cmd.equals("ProcessFile")) {
				ProcessFile.main(cmdArgs);
			} else if (cmd.equals("TransformRdf")) {
				TransformRdf.main(cmdArgs);
			} else if (cmd.equals("TransformLargeRdf")) {
				TransformLargeRdf.main(cmdArgs);
			} else if (cmd.equals("TransformNanopub")) {
				TransformNanopub.main(cmdArgs);
			} else if (cmd.equals("CheckNanopubViaSparql")) {
				CheckNanopubViaSparql.main(cmdArgs);
			} else {
				System.err.println("ERROR: Unrecognized command " + cmd);
				System.exit(1);
			}
			long t = System.nanoTime() - ms;
			System.out.println("Time in seconds: " + t/1000000000.0);
			System.out.println("---");
		}
		reader.close();
	}

}

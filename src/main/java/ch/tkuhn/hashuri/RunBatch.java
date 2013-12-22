package ch.tkuhn.hashuri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

import ch.tkuhn.hashuri.file.ProcessFile;
import ch.tkuhn.hashuri.rdf.CheckNanopubViaSparql;
import ch.tkuhn.hashuri.rdf.TransformLargeRdf;
import ch.tkuhn.hashuri.rdf.TransformNanopub;
import ch.tkuhn.hashuri.rdf.TransformRdf;

public class RunBatch {
	
	public static void main(String[] args) throws Exception {
		String batchFile = args[0];

		BufferedReader reader = new BufferedReader(new FileReader(batchFile));
		int startFrom = 0;

		File runningFile = new File(batchFile + ".running");
		if (runningFile.exists()) {
			startFrom = new Integer(readFile(runningFile)) + 1;
			System.out.println("===");
			System.out.println("RESUMING at line " + startFrom);
			System.out.println("===");
		} else {
			writeFile(runningFile, startFrom + "");
		}

		String line;
		int lineNumber = -1;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			lineNumber = lineNumber + 1;
			if (startFrom > lineNumber) continue;
			writeFile(runningFile, lineNumber + "");
			System.out.println("COMMAND: " + line);
			String cmd = line.replaceFirst("^([^ ]+) .*$", "$1");
			String[] cmdArgs = line.substring(line.indexOf(' ')+1).split("\\s+");
			long ns = System.nanoTime();
			try {
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
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (OutOfMemoryError err) {
				err.printStackTrace();
				System.exit(99);
			}
			long t = System.nanoTime() - ns;
			System.out.println("Time in seconds: " + t/1000000000.0);
			System.out.println("---");
		}
		reader.close();
		runningFile.delete();
	}

	static String readFile(File file) throws IOException {
		byte[] encoded = Files.readAllBytes(file.toPath());
		return Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
	}

	static void writeFile(File file, String content) throws IOException {
		Files.write(file.toPath(), content.getBytes());
	}

}

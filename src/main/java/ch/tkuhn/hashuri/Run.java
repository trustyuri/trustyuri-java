package ch.tkuhn.hashuri;

import java.util.Arrays;

import ch.tkuhn.hashuri.file.ProcessFile;
import ch.tkuhn.hashuri.rdf.CheckNanopubViaSparql;
import ch.tkuhn.hashuri.rdf.CheckSortedRdf;
import ch.tkuhn.hashuri.rdf.TransformLargeRdf;
import ch.tkuhn.hashuri.rdf.TransformNanopub;
import ch.tkuhn.hashuri.rdf.TransformRdf;

public class Run {

	public static void main(String[] args) throws Exception {
		run(args);
	}

	public static void run(String[] command) throws Exception {
		String cmd = command[0];
		String[] cmdArgs = Arrays.copyOfRange(command, 1, command.length);
		if (cmd.equals("CheckFile")) {
			CheckFile.main(cmdArgs);
		} else if (cmd.equals("ProcessFile")) {
			ProcessFile.main(cmdArgs);
		} else if (cmd.equals("TransformRdf")) {
			TransformRdf.main(cmdArgs);
		} else if (cmd.equals("TransformLargeRdf")) {
			TransformLargeRdf.main(cmdArgs);
		} else if (cmd.equals("CheckSortedRdf")) {
			CheckSortedRdf.main(cmdArgs);
		} else if (cmd.equals("TransformNanopub")) {
			TransformNanopub.main(cmdArgs);
		} else if (cmd.equals("CheckNanopubViaSparql")) {
			CheckNanopubViaSparql.main(cmdArgs);
		} else {
			System.err.println("ERROR: Unrecognized command " + cmd);
			System.exit(1);
		}
	}

}

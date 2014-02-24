package net.trustyuri;

import java.util.Arrays;

import net.trustyuri.file.ProcessFile;
import net.trustyuri.rdf.CheckLargeRdf;
import net.trustyuri.rdf.CheckNanopubViaSparql;
import net.trustyuri.rdf.CheckSortedRdf;
import net.trustyuri.rdf.TransformLargeRdf;
import net.trustyuri.rdf.TransformNanopub;
import net.trustyuri.rdf.TransformRdf;

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
		} else if (cmd.equals("CheckLargeRdf")) {
			CheckLargeRdf.main(cmdArgs);
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

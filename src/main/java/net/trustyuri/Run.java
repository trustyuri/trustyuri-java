package net.trustyuri;

import java.io.IOException;
import java.util.Arrays;

import org.openrdf.OpenRDFException;

import net.trustyuri.file.ProcessFile;
import net.trustyuri.rdf.CheckLargeRdf;
import net.trustyuri.rdf.CheckNanopubViaSparql;
import net.trustyuri.rdf.CheckSortedRdf;
import net.trustyuri.rdf.TransformLargeRdf;
import net.trustyuri.rdf.TransformNanopub;
import net.trustyuri.rdf.TransformRdf;

/**
 * This class can run the following commands by calling the respective class:
 * CheckFile, ProcessFile, CheckLargeRdf, TransformRdf, TransformLargeRdf,
 * CheckSortedRdf, TransformNanopub, and CheckNanopubViaSparql
 *
 * @author Tobias Kuhn
 */
public class Run {

	private Run() {}  // no instances allowed

	/**
	 * Interprets the arguments as a command to run, for example: {"ProcessFile", "file.txt"}
	 *
	 * @param args the command
	 */
	public static void main(String[] args) throws IOException, OpenRDFException, TrustyUriException {
		run(args);
	}

	/**
	 * Runs the given command, for example: {"ProcessFile", "file.txt"}
	 *
	 * @param command the command, as a String array
	 */
	public static void run(String[] command) throws IOException, OpenRDFException, TrustyUriException {
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

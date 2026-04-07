package net.trustyuri;

import net.trustyuri.file.ProcessFile;
import net.trustyuri.rdf.CheckLargeRdf;
import net.trustyuri.rdf.CheckSortedRdf;
import net.trustyuri.rdf.TransformLargeRdf;
import net.trustyuri.rdf.TransformRdf;
import org.eclipse.rdf4j.common.exception.RDF4JException;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class can run the following commands by calling the respective class:
 * CheckFile, ProcessFile, CheckLargeRdf, TransformRdf, TransformLargeRdf,
 * CheckSortedRdf, TransformNanopub, and CheckNanopubViaSparql
 *
 * @author Tobias Kuhn
 */
public class Run {

    private Run() {
    }  // no instances allowed

    /**
     * Interprets the arguments as a command to run, for example: {"ProcessFile", "file.txt"}
     *
     * @param args the command
     */
    public static void main(String[] args) throws IOException, RDF4JException, TrustyUriException {
        run(args);
    }

    /**
     * Runs the given command, for example: {"ProcessFile", "file.txt"}
     *
     * @param command the command, as a String array
     */
    public static void run(String[] command) throws IOException, RDF4JException, TrustyUriException {
        String cmd = command[0];
        String[] cmdArgs = Arrays.copyOfRange(command, 1, command.length);
        switch (cmd) {
            case "CheckFile" -> CheckFile.main(cmdArgs);
            case "ProcessFile" -> ProcessFile.main(cmdArgs);
            case "CheckLargeRdf" -> CheckLargeRdf.main(cmdArgs);
            case "TransformRdf" -> TransformRdf.main(cmdArgs);
            case "TransformLargeRdf" -> TransformLargeRdf.main(cmdArgs);
            case "CheckSortedRdf" -> CheckSortedRdf.main(cmdArgs);
            default -> {
                System.err.println("ERROR: Unrecognized command " + cmd);
                System.exit(1);
            }
        }
    }

}

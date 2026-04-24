package net.trustyuri;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Runs a batch of commands from a file, one per line.
 */
public class RunBatch {

    private static final Logger logger = LoggerFactory.getLogger(RunBatch.class);

    private RunBatch() {
    }  // no instances allowed

    /**
     * Runs a batch of commands from a file, one per line. Lines starting with # are ignored.
     *
     * @param args the first argument is the batch file to run
     * @throws IOException    if there is an error reading the batch file or the running file
     * @throws RDF4JException if there is an error running a command that uses RDF4J
     */
    public static void main(String[] args) throws IOException, RDF4JException {
        String batchFile = args[0];

        BufferedReader reader = new BufferedReader(new FileReader(batchFile));
        int startFrom = 0;

        File runningFile = new File(batchFile + ".running");
        if (runningFile.exists()) {
            startFrom = Integer.parseInt(readFile(runningFile)) + 1;
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
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            lineNumber = lineNumber + 1;
            if (startFrom > lineNumber) {
                continue;
            }
            writeFile(runningFile, lineNumber + "");
            System.out.println("COMMAND: " + line);
            String[] cmd = line.split("\\s+");
            long ns = System.nanoTime();
            try {
                Run.run(cmd);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } catch (OutOfMemoryError err) {
                logger.error(err.getMessage(), err);
                System.exit(99);
            }
            long t = System.nanoTime() - ns;
            System.out.println("Time in seconds: " + t / 1000000000.0);
            System.out.println("---");
        }
        reader.close();
        runningFile.delete();
    }

    /**
     * Reads the content of a file as a string.
     *
     * @param file the file to read
     * @return the content of the file as a string
     * @throws IOException if there is an error reading the file
     */
    static String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
    }

    /**
     * Writes a string to a file.
     *
     * @param file    the file to write to
     * @param content the content to write to the file
     * @throws IOException if there is an error writing to the file
     */
    static void writeFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

}

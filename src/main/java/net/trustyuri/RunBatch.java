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

public class RunBatch {

    private static final Logger logger = LoggerFactory.getLogger(RunBatch.class);

    private RunBatch() {
    }  // no instances allowed

    public static void main(String[] args) throws IOException, RDF4JException, TrustyUriException {
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
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            lineNumber = lineNumber + 1;
            if (startFrom > lineNumber) continue;
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

    static String readFile(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
    }

    static void writeFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

}

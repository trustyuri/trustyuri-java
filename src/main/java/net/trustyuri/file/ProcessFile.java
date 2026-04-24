package net.trustyuri.file;

import net.trustyuri.ArtifactCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class for processing a file by computing its artifact code and renaming it to include the artifact code in the filename.
 */
public class ProcessFile {

    private static final Logger logger = LoggerFactory.getLogger(ProcessFile.class);

    /**
     * Main method that takes a filename as an argument, processes the file, and renames it to include the artifact code in the filename.
     *
     * @param args the command-line arguments, where args[0] is the filename to process
     * @throws IOException if the file cannot be read or renamed
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            logger.error("No filename provided. Usage: ProcessFile <filename>");
            throw new IllegalArgumentException("Expected a filename as the first argument");
        }
        String filename = args[0];
        logger.info("Processing file from command line: {}", filename);
        File file = new File(filename);
        process(file);
    }

    /**
     * Process the given file by computing its artifact code and renaming it to include the artifact code in the filename.
     *
     * @param file the file to process
     * @throws IOException if the file cannot be read or renamed
     */
    public static void process(File file) throws IOException {
        logger.info("Processing file: {}", file.getAbsolutePath());
        if (!file.exists()) {
            logger.error("File does not exist: {}", file.getAbsolutePath());
            throw new IOException("File not found: " + file.getAbsolutePath());
        }
        String filename = file.getName();
        FileHasher hasher = new FileHasher();
        ArtifactCode ac = hasher.makeArtifactCode(new BufferedInputStream(new FileInputStream(file)));
        String ext = "";
        String base = filename;
        if (filename.matches(".+\\.[A-Za-z0-9\\-_]{0,20}")) {
            ext = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$2");
            base = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$1");
        }
        logger.debug("Parsed filename — base: '{}', extension: '{}'", base, ext);
        String glue = "";
        if (!base.isEmpty() && base.charAt(base.length() - 1) != '.') {
            glue = ".";
        }
        File hashFile = new File(file.getParentFile(), base + glue + ac.toString() + ext);
        logger.info("Renaming '{}' to '{}'", file.getName(), hashFile.getName());
        boolean renamed = file.renameTo(hashFile);
        if (!renamed) {
            logger.error("Failed to rename '{}' to '{}'", file.getAbsolutePath(), hashFile.getAbsolutePath());
            throw new IOException("Could not rename file to: " + hashFile.getAbsolutePath());
        }
        logger.info("File successfully renamed to: {}", hashFile.getName());
    }

}

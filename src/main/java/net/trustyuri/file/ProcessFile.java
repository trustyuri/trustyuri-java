package net.trustyuri.file;

import net.trustyuri.ArtifactCode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class for processing a file by computing its artifact code and renaming it to include the artifact code in the filename.
 */
public class ProcessFile {

    /**
     * Main method that takes a filename as an argument, processes the file, and renames it to include the artifact code in the filename.
     *
     * @param args the command-line arguments, where args[0] is the filename to process
     * @throws IOException if the file cannot be read or renamed
     */
    public static void main(String[] args) throws IOException {
        String filename = args[0];
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
        String filename = file.getName();
        FileHasher hasher = new FileHasher();
        ArtifactCode ac = hasher.makeArtifactCode(new BufferedInputStream(new FileInputStream(file)));
        String ext = "";
        String base = filename;
        if (filename.matches(".+\\.[A-Za-z0-9\\-_]{0,20}")) {
            ext = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$2");
            base = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$1");
        }
        String glue = "";
        if (base.length() > 0 && base.charAt(base.length() - 1) != '.') {
            glue = ".";
        }
        File hashFile = new File(file.getParentFile(), base + glue + ac.toString() + ext);
        file.renameTo(hashFile);
    }

}

package net.trustyuri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Reads and checks content from local files or URLs.
 *
 * @author Tobias Kuhn
 */
public class CheckFile {

    private static final Logger logger = LoggerFactory.getLogger(CheckFile.class);

    /**
     * Reads and checks the content of one or several URLs (in the form of trusty URIs) or local
     * files (in the form of trusty file names).
     *
     * @param args a list of URLs or file names
     * @throws IOException        if the content cannot be read
     * @throws TrustyUriException if any of the URIs is not a trusty URI or if any of the modules is unknown
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        for (String arg : args) {
            check(arg);
        }
    }

    /**
     * Check the content of a trusty URI (fetched from the web) or a trusty file (read from the
     * file system).
     *
     * @param fileOrUrl the file name or URL
     * @throws IOException        if the content cannot be read
     * @throws TrustyUriException if the URI is not a trusty URI or if the module is unknown
     */
    public static void check(String fileOrUrl) throws IOException, TrustyUriException {
        CheckFile c;
        try {
            URL url = new URL(fileOrUrl);
            c = new CheckFile(url);
        } catch (MalformedURLException ex) {
            c = new CheckFile(new File(fileOrUrl));
        }

        boolean valid = c.check();
        if (valid) {
            logger.info("Correct hash: {}", c.r.getArtifactCode());
        } else {
            logger.error("*** INCORRECT HASH ***");
        }
    }

    private TrustyUriResource r;

    /**
     * Creates a new object to check the content to be fetched from a URL.
     *
     * @param url the URL
     * @throws IOException if the content cannot be read
     */
    public CheckFile(URL url) throws IOException {
        r = new TrustyUriResource(url);
    }

    /**
     * Creates a new object to check the content to be read from a local file.
     *
     * @param file the local file from which the content is read
     * @throws IOException if the content cannot be read
     */
    public CheckFile(File file) throws IOException {
        r = new TrustyUriResource(file);
    }

    /**
     * Checks whether the content matches the hash of the trusty URI.
     *
     * @return true if the content matches the hash
     * @throws IOException        if the content cannot be read
     * @throws TrustyUriException if the URI is not a trusty URI or if the module is unknown
     */
    public boolean check() throws IOException, TrustyUriException {
        TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
        if (module == null) {
            throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
        }
        return module.hasCorrectHash(r);
    }

}

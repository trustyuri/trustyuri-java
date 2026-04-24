package net.trustyuri.rdf;

import com.google.code.externalsorting.ExternalSort;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

/**
 * Checks the hash of a large RDF file by sorting the statements in a temporary file and then reading them one by one.
 */
public class CheckLargeRdf {

    private static final Logger logger = LoggerFactory.getLogger(CheckLargeRdf.class);

    /**
     * Checks the hash of a large RDF file.
     *
     * @param args the first argument is the file to check
     * @throws IOException        if there is an error reading or writing files
     * @throws TrustyUriException if there is an error with the trusty URI (e.g. invalid format, hash mismatch)
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        File file = new File(args[0]);
        CheckLargeRdf t = new CheckLargeRdf(file);
        boolean valid = t.check();
        if (valid) {
            logger.info("Hash is correct for file: {}", file.getAbsolutePath() + " with artifact code: " + t.ac.toString());
        } else {
            logger.error("Hash is incorrect for file: {}", file.getAbsolutePath());
        }
    }

    private File file;
    private MessageDigest md;
    private ArtifactCode ac;

    /**
     * Creates a new instance of CheckLargeRdf with the specified file.
     *
     * @param file the RDF file to check
     */
    public CheckLargeRdf(File file) {
        this.file = file;
    }

    /**
     * Checks the hash of the RDF file.
     *
     * @return true if the hash is correct, false otherwise
     * @throws IOException        if there is an error reading or writing files
     * @throws TrustyUriException if there is an error with the trusty URI
     */
    public boolean check() throws IOException, TrustyUriException {
        TrustyUriResource r = new TrustyUriResource(file);
        File dir = file.getParentFile();
        String fileName = file.getName();
        md = RdfHasher.getDigest();
        RDFFormat format = r.getFormat(RDFFormat.TURTLE);

        RDFParser p = RdfUtils.getParser(format);
        File sortInFile = new File(dir, fileName + ".temp.sort-in");
        final FileOutputStream preOut = new FileOutputStream(sortInFile);
        p.setRDFHandler(new RdfPreprocessor(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                String s = SerStatementComparator.toString(st) + "\n";
                try {
                    preOut.write(s.getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    logger.error("Error writing to temporary file for artifact code {}: {}", r.getArtifactCode(), ex.getMessage());
                }
            }

        }, r.getArtifactCode()));
        BufferedReader reader = new BufferedReader(r.getInputStreamReader(), 64 * 1024);
        try {
            p.parse(reader, "");
        } catch (RDF4JException ex) {
            throw new TrustyUriException(ex);
        } finally {
            reader.close();
            preOut.close();
        }

        File sortOutFile = new File(dir, fileName + ".temp.sort-out");
        File sortTempDir = new File(dir, fileName + ".temp");
        sortTempDir.mkdir();
        Comparator<String> cmp = new SerStatementComparator();
        Charset cs = Charset.defaultCharset();
        System.gc();
        List<File> tempFiles = ExternalSort.sortInBatch(sortInFile, cmp, 1024, cs, sortTempDir, false);
        ExternalSort.mergeSortedFiles(tempFiles, sortOutFile, cmp, cs);
        sortInFile.delete();
        sortTempDir.delete();

        BufferedReader br = new BufferedReader(new FileReader(sortOutFile));
        String line;
        Statement previous = null;
        while ((line = br.readLine()) != null) {
            Statement st = SerStatementComparator.fromString(line);
            if (!st.equals(previous)) {
                RdfHasher.digest(st, md);
            }
            previous = st;
        }
        br.close();
        sortOutFile.delete();

        ac = RdfHasher.getArtifactCode(md);
        return ac.toString().equals(r.getArtifactCode());
    }

}

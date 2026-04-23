package net.trustyuri.rdf;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Checks that the RDF file is sorted and that the hash is correct.
 */
public class CheckSortedRdf {

    private static final Logger logger = LoggerFactory.getLogger(CheckSortedRdf.class);

    /**
     * Checks that the RDF file is sorted and that the hash is correct.
     *
     * @param args the first argument is the file to check
     * @throws IOException        if there is an error reading the file
     * @throws TrustyUriException if there is an error with the trusty URI (e.g. not a trusty URI, unsupported module, etc.)
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        File file = new File(args[0]);
        CheckSortedRdf ch = new CheckSortedRdf(file);
        boolean isCorrect = ch.check();
        if (isCorrect) {
            logger.info("Correct hash: {}", ch.getArtifactCode().toString());
        } else {
            logger.error("*** INCORRECT HASH ***");
        }
    }

    private File file;
    private MessageDigest md;
    private Statement previous;
    private TrustyUriResource r;

    /**
     * Creates a new CheckSortedRdf instance for the given file.
     *
     * @param file the file to check
     */
    public CheckSortedRdf(File file) {
        this.file = file;
    }

    /**
     * Checks that the RDF file is sorted and that the hash is correct.
     *
     * @return true if the hash is correct, false otherwise
     * @throws IOException        if there is an error reading the file
     * @throws TrustyUriException if there is an error with the trusty URI (e.g. not a trusty URI, unsupported module, etc.)
     */
    public boolean check() throws IOException, TrustyUriException {
        md = RdfHasher.getDigest();
        r = new TrustyUriResource(file);
        if (r.getArtifactCode() == null) {
            logger.error("ERROR: Not a trusty URI or unknown module");
            System.exit(1);
        }
        String moduleId = r.getModuleId();
        if (!moduleId.equals(RdfModule.MODULE_ID)) {
            logger.error("ERROR: Unsupported module: {} (this function only supports " + RdfModule.MODULE_ID + ")", moduleId);
            System.exit(1);
        }
        RDFFormat format = r.getFormat(RDFFormat.TURTLE);

        RDFParser p = RdfUtils.getParser(format);
        previous = null;
        p.setRDFHandler(new RdfPreprocessor(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                if (previous != null && StatementComparator.compareStatement(previous, st) > 0) {
                    throw new RuntimeException("File not sorted");
                }
                if (!st.equals(previous)) {
                    RdfHasher.digest(st, md);
                }
                previous = st;
            }

        }, r.getArtifactCode()));
        BufferedReader reader = new BufferedReader(r.getInputStreamReader(), 64 * 1024);
        try {
            p.parse(reader, "");
        } catch (RDF4JException ex) {
            throw new TrustyUriException(ex);
        } finally {
            reader.close();
        }

        ArtifactCode artifactCode = RdfHasher.getArtifactCode(md);
        return artifactCode.toString().equals(r.getArtifactCode());
    }

    /**
     * Returns the artifact code of the nanopublication contained in the file.
     *
     * @return the artifact code of the nanopublication contained in the file
     */
    public ArtifactCode getArtifactCode() {
        return ArtifactCode.of(r.getArtifactCode());
    }

}
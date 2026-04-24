package net.trustyuri.rdf;

import com.google.code.externalsorting.ExternalSort;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * This class can be used to transform a large RDF file.
 */
public class TransformLargeRdf {

    private static final Logger logger = LoggerFactory.getLogger(TransformLargeRdf.class);

    /**
     * Transforms the given RDF file.
     *
     * @param args the first argument is the RDF file to transform, the second argument is the base name for the output file (optional, default: the input file name without extension)
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a valid RDF file or if the base name is invalid
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        File inputFile = new File(args[0]);
        String baseName;
        if (args.length > 1) {
            baseName = args[1];
        } else {
            baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        }
        TransformLargeRdf t = new TransformLargeRdf(inputFile, baseName);
        t.transform(TransformRdfSetting.defaultSetting);
    }

    private File inputFile;
    private String inputDir;
    private String baseName;
    private MessageDigest md;
    private IRI baseUri;
    private String fileName, ext;

    /**
     * Creates a new TransformLargeRdf object for the given RDF file and base name.
     *
     * @param inputFile the RDF file to transform
     * @param baseName  the base name for the output file
     */
    public TransformLargeRdf(File inputFile, String baseName) {
        this.inputFile = inputFile;
        this.baseName = baseName;
    }

    /**
     * Transforms the RDF file.
     *
     * @param setting the setting to use for the transformation
     * @return the trusty URI of the transformed RDF file
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a valid RDF file or if the base name is invalid
     */
    public IRI transform(TransformRdfSetting setting) throws IOException, TrustyUriException {
        logger.info("Starting RDF transformation: input='{}', baseName='{}'", inputFile, baseName);
        baseUri = TransformRdf.getBaseURI(baseName);
        md = RdfHasher.getDigest();
        inputDir = inputFile.getParent();
        TrustyUriResource r = new TrustyUriResource(inputFile);
        RDFFormat format = r.getFormat(RDFFormat.TURTLE);
        logger.info("Detected RDF format: {}", format);

        String name = baseName;
        if (baseName.indexOf("/") > 0) {
            name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
        }
        fileName = name;
        ext = "";
        if (!format.getFileExtensions().isEmpty()) {
            ext = "." + format.getFileExtensions().getFirst();
        }

        RDFParser p = RdfUtils.getParser(format);
        File sortInFile = new File(inputDir, fileName + ".temp.sort-in");
        logger.info("Preprocessing RDF into sortable form: {}", sortInFile);

        final FileOutputStream preOut = new FileOutputStream(sortInFile);
        p.setRDFHandler(new RdfPreprocessor(new AbstractRDFHandler() {

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                String s = SerStatementComparator.toString(st) + "\n";
                try {
                    preOut.write(s.getBytes());
                } catch (IOException ex) {
                    logger.error("Failed to write preprocessed statement to temp file: {}", sortInFile, ex);
                    throw new RDFHandlerException("Error writing preprocessed RDF", ex);
                }
            }

        }, baseUri, setting));
        BufferedReader reader = new BufferedReader(r.getInputStreamReader(), 64 * 1024);
        try {
            p.parse(reader, "");
        } catch (RDF4JException ex) {
            logger.error("Failed to parse RDF input file: {}", inputFile, ex);
            throw new TrustyUriException(ex);
        } finally {
            reader.close();
            preOut.close();
        }

        File sortOutFile = new File(inputDir, fileName + ".temp.sort-out");
        File sortTempDir = new File(inputDir, fileName + ".temp");
        if (!sortTempDir.mkdir()) {
            logger.warn("Could not create temp directory: {}", sortTempDir);
        }

        logger.info("Sorting statements (external sort)...");
        Comparator<String> cmp = new SerStatementComparator();
        Charset cs = Charset.defaultCharset();
        System.gc();
        List<File> tempFiles = ExternalSort.sortInBatch(sortInFile, cmp, 1024, cs, sortTempDir, false);
        logger.info("Created {} sorted temp files", tempFiles.size());

        ExternalSort.mergeSortedFiles(tempFiles, sortOutFile, cmp, cs);
        if (!sortInFile.delete()) {
            logger.warn("Failed to delete temp file: {}", sortInFile);
        }
        if (!sortTempDir.delete()) {
            logger.warn("Failed to delete temp dir: {}", sortTempDir);
        }

        logger.info("Hashing sorted statements...");
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
        logger.info("Hashing completed.");

        ArtifactCode artifactCode = RdfHasher.getArtifactCode(md);
        String acFileName = fileName;
        if (acFileName.isEmpty()) {
            acFileName = artifactCode + ext;
        } else {
            acFileName += "." + artifactCode + ext;
        }
        File outputFile;
        OutputStream out;
        if (inputFile.getName().matches(".*\\.(gz|gzip)")) {
            outputFile = new File(inputDir, acFileName + ".gz");
            logger.info("Writing compressed output: {}", outputFile);
            out = new GZIPOutputStream(new FileOutputStream(outputFile));
        } else {
            outputFile = new File(inputDir, acFileName);
            logger.info("Writing output: {}", outputFile);
            out = new FileOutputStream(outputFile);
        }
        RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(out, StandardCharsets.UTF_8));
        final HashAdder replacer = new HashAdder(baseUri, artifactCode, writer, null);

        br = new BufferedReader(new FileReader(sortOutFile));
        try {
            replacer.startRDF();
            previous = null;
            while ((line = br.readLine()) != null) {
                Statement st = SerStatementComparator.fromString(line);
                if (!st.equals(previous)) {
                    replacer.handleStatement(st);
                }
                previous = st;
            }
            replacer.endRDF();
        } catch (RDFHandlerException ex) {
            logger.error("Failed during RDF writing phase", ex);
            throw new TrustyUriException(ex);
        } finally {
            br.close();
            if (!sortOutFile.delete()) {
                logger.warn("Failed to delete temp file: {}", sortOutFile);
            }
        }
        out.close();
        return RdfUtils.getTrustyUri(baseUri, artifactCode.toString(), setting);
    }

}

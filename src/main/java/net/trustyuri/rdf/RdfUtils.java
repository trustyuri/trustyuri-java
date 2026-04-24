package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import net.trustyuri.TrustyUriUtils;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * This class is used for various utility functions related to RDF processing.
 */
public class RdfUtils {

    private static final Logger logger = LoggerFactory.getLogger(RdfUtils.class);

    private RdfUtils() {
    }  // no instances allowed

    /**
     * Creates a trusty URI string based on the given base URI, artifact code, and suffix.
     *
     * @param baseUri      the base URI to use for the trusty URI
     * @param artifactCode the artifact code to include in the trusty URI
     * @param suffix       the suffix to append to the trusty URI (can be null)
     * @param setting      the settings to use for generating the trusty URI
     * @return the generated trusty URI string
     */
    public static String getTrustyUriString(IRI baseUri, String artifactCode, String suffix, TransformRdfSetting setting) {
        String s = expandBaseUri(baseUri, setting) + artifactCode;
        if (suffix != null) {
            suffix = suffix.replace("#", "%23");
            if (suffix.startsWith(setting.getBnodeChar() + "")) {
                // Duplicate bnode character for escaping:
                s += "" + getPostAcChar(baseUri, setting) + setting.getBnodeChar() + suffix;
            } else {
                s += "" + getPostAcChar(baseUri, setting) + suffix;
            }
        }
        logger.debug("Generated trusty URI string: '{}'", s);
        return s;
    }

    /**
     * Creates a trusty URI string based on the given base URI, artifact code, and suffix.
     *
     * @param baseUri      the base URI to use for the trusty URI
     * @param artifactCode the artifact code to include in the trusty URI
     * @param setting      the settings to use for generating the trusty URI
     * @return the generated trusty URI string
     */
    public static String getTrustyUriString(IRI baseUri, String artifactCode, TransformRdfSetting setting) {
        return getTrustyUriString(baseUri, artifactCode, null, setting);
    }

    /**
     * Creates a trusty URI based on the given base URI, artifact code, and suffix.
     *
     * @param baseUri      the base URI to use for the trusty URI
     * @param artifactCode the artifact code to include in the trusty URI
     * @param suffix       the suffix to append to the trusty URI (can be null)
     * @param setting      the settings to use for generating the trusty URI
     * @return the generated trusty URI, or null if the base URI is null
     */
    public static IRI getTrustyUri(IRI baseUri, String artifactCode, String suffix, TransformRdfSetting setting) {
        if (baseUri == null) {
            logger.debug("getTrustyUri called with null baseUri, returning null");
            return null;
        }
        return SimpleValueFactory.getInstance().createIRI(getTrustyUriString(baseUri, artifactCode, suffix, setting));
    }

    /**
     * Creates a trusty URI based on the given base URI, artifact code, and suffix.
     *
     * @param baseUri      the base URI to use for the trusty URI
     * @param artifactCode the artifact code to include in the trusty URI
     * @param setting      the settings to use for generating the trusty URI
     * @return the generated trusty URI, or null if the base URI is null
     */
    public static IRI getTrustyUri(IRI baseUri, String artifactCode, TransformRdfSetting setting) {
        if (baseUri == null) {
            logger.debug("getTrustyUri called with null baseUri, returning null");
            return null;
        }
        return SimpleValueFactory.getInstance().createIRI(getTrustyUriString(baseUri, artifactCode, setting));
    }

    /**
     * Gets the pre-URI for a given RDF resource.
     *
     * @param resource the RDF resource for which to get the pre-URI
     * @param baseUri  the base URI to use for generating the pre-URI
     * @param bnodeMap a map to keep track of blank node identifiers and their corresponding numbers
     * @param frozen   a boolean indicating whether to return null for blank nodes (if true) or to generate skolemized URIs (if false)
     * @param setting  the settings to use for generating the pre-URI
     * @return the pre-URI for the given RDF resource, or null if the resource is a blank node and frozen is true
     */
    public static IRI getPreUri(Resource resource, IRI baseUri, Map<String, Integer> bnodeMap, boolean frozen, TransformRdfSetting setting) {
        if (resource == null) {
            logger.error("getPreUri called with null resource");
            throw new RuntimeException("Resource is null");
        } else if (resource instanceof IRI) {
            IRI plainUri = (IRI) resource;
            checkUri(plainUri);
            // TODO Add option to disable suffixes appended to trusty URIs
            String suffix = getSuffix(plainUri, baseUri);
            if (suffix == null && !plainUri.equals(baseUri)) {
                logger.debug("URI '{}' is outside base URI scope, leaving as-is after placeholder substitution", plainUri);
                return SimpleValueFactory.getInstance().createIRI(plainUri.stringValue().replaceAll("~~~ARTIFACTCODE~~~", " "));
            } else if (frozen) {
                logger.debug("URI '{}' would modify a frozen trusty graph, returning null", plainUri);
                return null;
            } else if (TrustyUriUtils.isPotentialTrustyUri(plainUri)) {
                logger.debug("URI '{}' is already a trusty URI, leaving unchanged", plainUri);
                return plainUri;
            } else {
                logger.debug("Generating trusty pre-URI for '{}' with suffix '{}'", plainUri, suffix);
                return getTrustyUri(baseUri, " ", suffix, setting);
            }
        } else {
            if (frozen) {
                logger.debug("Blank node '{}' in frozen context, returning null", resource);
                return null;
            } else {
                IRI skolemized = getSkolemizedUri((BNode) resource, baseUri, bnodeMap, setting);
                logger.debug("Skolemized blank node '{}' to '{}'", resource, skolemized);
                return skolemized;
            }
        }
    }

    /**
     * Checks if the given URI is well-formed by attempting to create a java.net.URI object from its string value.
     *
     * @param uri the IRI to check for well-formedness
     */
    public static void checkUri(IRI uri) {
        try {
            new URI(uri.stringValue());
        } catch (URISyntaxException ex) {
            logger.error("Malformed URI encountered: '{}'", uri.stringValue());
            throw new RuntimeException("Malformed URI: " + uri.stringValue(), ex);
        }
    }

    /**
     * Gets the character to use for separating the artifact code from the suffix in a trusty URI, based on the given base URI and settings.
     *
     * @param baseUri the base URI to check for the presence of the default post-artifact-code character
     * @param setting the settings to use for determining the post-artifact-code character and fallback character
     * @return the character to use for separating the artifact code from the suffix in a trusty URI
     */
    public static char getPostAcChar(IRI baseUri, TransformRdfSetting setting) {
        if (setting.getPostAcChar() == '#' && baseUri.stringValue().contains("#")) {
            logger.debug("Base URI '{}' already contains '#', using fallback post-AC char '{}'", baseUri, setting.getPostAcFallbackChar());
            return setting.getPostAcFallbackChar();
        }
        return setting.getPostAcChar();
    }

    private static IRI getSkolemizedUri(BNode bnode, IRI baseUri, Map<String, Integer> bnodeMap, TransformRdfSetting setting) {
        int n = getBlankNodeNumber(bnode, bnodeMap);
        return SimpleValueFactory.getInstance().createIRI(expandBaseUri(baseUri, setting) + " " + getPostAcChar(baseUri, setting) + setting.getBnodeChar() + n);
    }

    private static String getSuffix(IRI plainUri, IRI baseUri) {
        if (baseUri == null) {
            return null;
        }
        String b = baseUri.toString();
        String p = plainUri.toString();
        if (p.equals(b)) {
            return null;
        } else if (p.startsWith(b)) {
            return p.substring(b.length());
        } else {
            return null;
        }
    }

    /**
     * Normalizes a URI by replacing the artifact code with a space and checking for newline or tab characters.
     *
     * @param uri          the URI to normalize
     * @param artifactCode the artifact code to replace with a space (can be null)
     * @return the normalized URI string
     */
    public static String normalize(IRI uri, String artifactCode) {
        String s = uri.toString();
        if (s.indexOf('\n') > -1 || s.indexOf('\t') > -1) {
            throw new RuntimeException("Newline or tab character in URI: " + s);
        }
        if (artifactCode == null) {
            return s;
        }
        return s.replace(artifactCode, " ");
    }

    private static int getBlankNodeNumber(BNode blankNode, Map<String, Integer> bnodeMap) {
        String id = blankNode.getID();
        Integer n = bnodeMap.get(id);
        if (n == null) {
            n = bnodeMap.size() + 1;
            bnodeMap.put(id, n);
            logger.debug("Assigned blank node '{}' skolem number {}", id, n);
        }
        return n;
    }

    private static String expandBaseUri(IRI baseUri, TransformRdfSetting setting) {
        String s = baseUri.toString();

        // Deprecated (we should only use "~~~ARTIFACTCODE~~~" in the future:
        s = s.replaceFirst("ARTIFACTCODE-PLACEHOLDER[\\.#/]?$", "");

        // TODO Include this in test cases:
        s = s.replaceFirst("~~~ARTIFACTCODE~~~[\\.#/]?$", "");
        if (s.matches(".*[A-Za-z0-9\\-_]")) {
            s += setting.getPreAcChar();
        }
        logger.debug("Expanded base URI '{}' to '{}'", baseUri, s);
        return s;
    }

    /**
     * Loads RDF content from the given input stream and format, and returns it as an RdfFileContent object.
     *
     * @param in     the input stream to read the RDF content from
     * @param format the RDF format of the content to load
     * @return an RdfFileContent object containing the loaded RDF content
     * @throws IOException        if there is an error reading from the input stream
     * @throws TrustyUriException if there is an error parsing the RDF content, for example if the content is not well-formed or if there are issues with the URIs in the content
     */
    public static RdfFileContent load(InputStream in, RDFFormat format) throws IOException, TrustyUriException {
        logger.debug("Loading RDF content in format '{}'", format.getName());
        RDFParser p = getParser(format);
        RdfFileContent content = new RdfFileContent(format);
        p.setRDFHandler(content);
        try {
            p.parse(new InputStreamReader(in, StandardCharsets.UTF_8), "");
        } catch (RDF4JException ex) {
            logger.error("Failed to parse RDF content in format '{}': {}", format.getName(), ex.getMessage());
            throw new TrustyUriException(ex);
        } finally {
            in.close();
        }
        logger.debug("Loaded {} statements from RDF content", content.getStatements().size());
        return content;
    }

    /**
     * Creates and configures an RDFParser for the given RDF format.
     *
     * @param format the RDF format for which to create the parser
     * @return a configured RDFParser for the given RDF format
     */
    public static RDFParser getParser(RDFFormat format) {
        RDFParser p = Rio.createParser(format);
        p.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_URI_SYNTAX);
        p.getParserConfig().set(BasicParserSettings.NAMESPACES, new HashSet<Namespace>());
        return p;
    }

    /**
     * Loads RDF content from the given TrustyUriResource and returns it as an RdfFileContent object.
     *
     * @param r the TrustyUriResource to load the RDF content from
     * @return an RdfFileContent object containing the loaded RDF content
     * @throws IOException        if there is an error reading from the resource
     * @throws TrustyUriException if there is an error parsing the RDF content, for example if the content is not well-formed or if there are issues with the URIs in the content
     */
    public static RdfFileContent load(TrustyUriResource r) throws IOException, TrustyUriException {
        logger.debug("Loading RDF content from resource: '{}'", r);
        return load(r.getInputStream(), r.getFormat(RDFFormat.TURTLE));
    }

    /**
     * Fixes the given trusty RDF file by recalculating the artifact code based on the content and updating the URIs accordingly.
     *
     * @param file the trusty RDF file to fix
     * @throws IOException        if there is an error reading or writing the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public static void fixTrustyRdf(File file) throws IOException, TrustyUriException {
        logger.info("Fixing trusty RDF file: '{}'", file.getAbsolutePath());
        TrustyUriResource r = new TrustyUriResource(file);
        RdfFileContent content = RdfUtils.load(r);
        ArtifactCode oldArtifactCode = ArtifactCode.of(r.getArtifactCode());
        logger.debug("Old artifact code: '{}'", oldArtifactCode);
        content = RdfPreprocessor.run(content, oldArtifactCode.toString());
        ArtifactCode newArtifactCode = createArtifactCode(content, oldArtifactCode.getModule().getModuleId().equals(RdfGraphModule.MODULE_ID));
        logger.info("Replacing artifact code '{}' with '{}' in '{}'", oldArtifactCode, newArtifactCode, file.getName());
        content = processNamespaces(content, oldArtifactCode, newArtifactCode);
        OutputStream out;
        String filename = r.getFilename().replace(oldArtifactCode.toString(), newArtifactCode.toString());
        if (filename.matches(".*\\.(gz|gzip)")) {
            out = new GZIPOutputStream(new FileOutputStream("fixed." + filename));
        } else {
            out = new FileOutputStream("fixed." + filename);
        }
        logger.debug("Writing fixed RDF content to: 'fixed.{}'", filename);
        RDFWriter writer = Rio.createWriter(r.getFormat(RDFFormat.TRIG), new OutputStreamWriter(out, StandardCharsets.UTF_8));
        TransformRdf.transformPreprocessed(content, null, writer, null);
        logger.info("Successfully wrote fixed file: 'fixed.{}'", filename);
    }

    /**
     * Fixes the given trusty RDF file by recalculating the artifact code based on the content and updating the URIs accordingly.
     *
     * @param content         the RdfFileContent object containing the RDF content to fix
     * @param oldArtifactCode the old artifact code to replace in the RDF content
     * @param writer          the RDFHandler to write the fixed RDF content to
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public static void fixTrustyRdf(RdfFileContent content, ArtifactCode oldArtifactCode, RDFHandler writer)
            throws TrustyUriException {
        logger.debug("Fixing trusty RDF content with artifact code '{}'", oldArtifactCode);
        content = RdfPreprocessor.run(content, oldArtifactCode.toString());
        ArtifactCode newArtifactCode = createArtifactCode(content, oldArtifactCode.getModule().getModuleId().equals(RdfGraphModule.MODULE_ID));
        logger.info("Replacing artifact code '{}' with '{}'", oldArtifactCode, newArtifactCode);
        content = processNamespaces(content, oldArtifactCode, newArtifactCode);
        TransformRdf.transformPreprocessed(content, null, writer, null);
    }

    private static ArtifactCode createArtifactCode(RdfFileContent preprocessedContent, boolean graphModule) throws TrustyUriException {
        logger.debug("Creating artifact code using {} module over {} statements", graphModule ? "RdfGraph" : "Rdf", preprocessedContent.getStatements().size());
        if (graphModule) {
            return RdfHasher.makeGraphArtifactCode(preprocessedContent.getStatements());
        } else {
            return RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
        }
    }

    private static RdfFileContent processNamespaces(RdfFileContent content, ArtifactCode oldArtifactCode, ArtifactCode newArtifactCode) {
        try {
            RdfFileContent contentOut = new RdfFileContent(content.getOriginalFormat());
            content.propagate(new NamespaceProcessor(oldArtifactCode, newArtifactCode, contentOut));
            return contentOut;
        } catch (RDFHandlerException ex) {
            logger.error("Failed to process namespaces while replacing artifact code '{}' with '{}': {}", oldArtifactCode, newArtifactCode, ex.getMessage(), ex);
        }
        return content;
    }

    private static class NamespaceProcessor implements RDFHandler {

        private RDFHandler handler;
        private ArtifactCode oldArtifactCode, newArtifactCode;

        /**
         * Creates a new NamespaceProcessor that replaces the old artifact code with the new artifact code in namespace URIs.
         *
         * @param oldArtifactCode the old artifact code to replace in namespace URIs
         * @param newArtifactCode the new artifact code to use in namespace URIs
         * @param handler         the RDFHandler to which the processed RDF content will be passed
         */
        public NamespaceProcessor(ArtifactCode oldArtifactCode, ArtifactCode newArtifactCode, RDFHandler handler) {
            this.handler = handler;
            this.oldArtifactCode = oldArtifactCode;
            this.newArtifactCode = newArtifactCode;
        }

        @Override
        public void startRDF() throws RDFHandlerException {
            handler.startRDF();
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            handler.endRDF();
        }

        @Override
        public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
            String updated = uri.replace(oldArtifactCode.toString(), newArtifactCode.toString());
            if (!updated.equals(uri)) {
                logger.debug("Updated namespace '{}': '{}' → '{}'", prefix, uri, updated);
            }
            handler.handleNamespace(prefix, updated);
        }

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            handler.handleStatement(st);
        }

        @Override
        public void handleComment(String comment) throws RDFHandlerException {
            handler.handleComment(comment);
        }

    }

}

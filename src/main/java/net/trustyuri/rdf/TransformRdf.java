package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Transforms RDF content into trusty URI format.
 */
public class TransformRdf {

    // TODO Use RB module by default if trusty URI represents a single RDF graph

    /**
     * Transforms the RDF file given as the first argument into trusty URI format and saves it in the same directory. The second argument is an optional base name for the output file, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param args the first argument is the input file, the second argument is an optional base name for the output file, which can also be a base URI
     * @throws IOException        if an I/O error occurs
     * @throws TrustyUriException if the transformation fails due to an invalid input or other issues
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        File inputFile = new File(args[0]);
        String baseName = null;
        if (args.length > 1) {
            baseName = args[1];
        }
        transform(inputFile, baseName, TransformRdfSetting.defaultSetting);
    }

    /**
     * Transforms the RDF file into trusty URI format and saves it in the same directory. The base name for the output file can be given as an argument, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param inputFile the RDF file to be transformed
     * @param baseName  an optional base name for the output file, which can also be a base URI; if null, the base name is derived from the input file name by removing the extension
     * @param setting   the settings for the transformation, which can include options for handling blank nodes, literals, and other aspects of the RDF content
     * @return the IRI of the transformed RDF content in trusty URI format
     * @throws IOException        if an I/O error occurs during reading the input file or writing the output file
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing
     */
    public static IRI transform(File inputFile, String baseName, TransformRdfSetting setting)
            throws IOException, TrustyUriException {
        if (baseName == null) {
            baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        }
        RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
        IRI baseUri = getBaseURI(baseName);
        String name = baseName;
        if (baseName.indexOf("/") > 0) {
            name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
        }

        content = RdfPreprocessor.run(content, baseUri, setting);
        ArtifactCode artifactCode = RdfHasher.makeArtifactCode(content.getStatements());
        RDFFormat format = content.getOriginalFormat();
        String fileName = name;
        String ext = "";
        if (!format.getFileExtensions().isEmpty()) {
            ext = "." + format.getFileExtensions().getFirst();
        }
        if (fileName.isEmpty()) {
            fileName = artifactCode + ext;
        } else {
            fileName += "." + artifactCode + ext;
        }
        OutputStream out;
        if (inputFile.getName().matches(".*\\.(gz|gzip)")) {
            out = new GZIPOutputStream(new FileOutputStream(new File(inputFile.getParent(), fileName + ".gz")));
        } else {
            out = new FileOutputStream(new File(inputFile.getParent(), fileName));
        }
        RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(out, StandardCharsets.UTF_8));
        IRI uri = includeArtifactCode(content, artifactCode, baseUri, writer, setting);
        out.close();
        return uri;
    }

    /**
     * Transforms the RDF content into trusty URI format and writes it using the provided RDFHandler. The base name for the output can be given as an argument, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param content  the RDF content to be transformed, which can be loaded from a file or created programmatically; it should contain the original format of the RDF data for proper processing
     * @param handler  the RDFHandler to which the transformed RDF statements will be written; this can be an RDFWriter for writing to a file or an RDFHandler for processing the statements in memory
     * @param baseName an optional base name for the output, which can also be a base URI; if null, the base name is derived from the input file name by removing the extension; this base name is used to construct the trusty URI and may affect how blank nodes and other elements are handled during transformation
     * @param setting  the settings for the transformation, which can include options for handling blank nodes, literals, and other aspects of the RDF content; these settings may influence how the trusty URI is generated and how the RDF statements are processed during transformation
     * @return the IRI of the transformed RDF content in trusty URI format, which is constructed based on the base name and the artifact code generated from the RDF content; this IRI can be used to reference the transformed RDF data and may be included in the output RDF statements as needed
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided handler
     */
    public static IRI transform(RdfFileContent content, RDFHandler handler, String baseName, TransformRdfSetting setting)
            throws TrustyUriException {
        IRI baseUri = getBaseURI(baseName);
        content = RdfPreprocessor.run(content, baseUri, setting);
        return transformPreprocessed(content, baseUri, handler, setting);
    }

    /**
     * Transforms the RDF content into trusty URI format and writes it using the provided RDFHandler.
     *
     * @param content  the RDF content to be transformed
     * @param handler  the RDFHandler to which the transformed RDF statements will be written
     * @param baseName the base name for the output
     * @param setting  the settings for the transformation
     * @return a map containing the resources and their corresponding IRIs in the transformed RDF content
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided handler
     */
    public static Map<Resource, IRI> transformAndGetMap(RdfFileContent content, RDFHandler handler, String baseName, TransformRdfSetting setting)
            throws TrustyUriException {
        IRI baseUri = getBaseURI(baseName);
        RdfFileContent newContent = new RdfFileContent(content.getOriginalFormat());
        RdfPreprocessor rp = new RdfPreprocessor(newContent, baseUri, setting);
        try {
            content.propagate(rp);
        } catch (RDFHandlerException ex) {
            throw new TrustyUriException(ex);
        }
        ArtifactCode artifactCode = RdfHasher.makeArtifactCode(newContent.getStatements());
        includeArtifactCode(newContent, artifactCode, baseUri, handler, setting);
        return finalizeTransformMap(rp.getTransformMap(), artifactCode);
    }

    /**
     * Transforms the RDF content into trusty URI format and writes it using the provided RDFHandler. The base name for the output can be given as an argument, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param in       the InputStream from which the RDF content will be read
     * @param format   the RDFFormat of the input RDF content
     * @param out      the OutputStream to which the transformed RDF statements will be written
     * @param baseName an optional base name for the output, which can also be a base URI; if null, the base name is derived from the input file name by removing the extension; this base name is used to construct the trusty URI and may affect how blank nodes and other elements are handled during transformation
     * @param setting  the settings for the transformation, which can include options for handling blank nodes, literals, and other aspects of the RDF content; these settings may influence how the trusty URI is generated and how the RDF statements are processed during transformation
     * @return the IRI of the transformed RDF content in trusty URI format, which is constructed based on the base name and the artifact code generated from the RDF content; this IRI can be used to reference the transformed RDF data and may be included in the output RDF statements as needed
     * @throws IOException        if an I/O error occurs during reading the input or writing the output
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided handler
     */
    public static IRI transform(InputStream in, RDFFormat format, OutputStream out, String baseName, TransformRdfSetting setting)
            throws IOException, TrustyUriException {
        IRI baseUri = getBaseURI(baseName);
        RdfFileContent content = RdfUtils.load(in, format);
        content = RdfPreprocessor.run(content, baseUri, setting);
        RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(out, StandardCharsets.UTF_8));
        IRI uri = transformPreprocessed(content, baseUri, writer, setting);
        out.close();
        return uri;
    }

    /**
     * Transforms the preprocessed RDF content into trusty URI format and writes it using the provided RDFWriter. The base name for the output can be given as an argument, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param preprocessedContent the RDF content that has already been preprocessed, which means it has been processed to handle blank nodes, literals, and other aspects according to the specified settings; this content should be ready for generating the artifact code and constructing the trusty URI without needing further preprocessing steps
     * @param baseUri             the base URI to be used
     * @param writer              the RDFWriter to which the transformed RDF statements will be written
     * @param setting             the settings for the transformation
     * @return the IRI of the transformed RDF content in trusty URI format
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided writer
     */
    public static IRI transformPreprocessed(RdfFileContent preprocessedContent, IRI baseUri, RDFWriter writer, TransformRdfSetting setting)
            throws TrustyUriException {
        ArtifactCode artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
        return includeArtifactCode(preprocessedContent, artifactCode, baseUri, writer, setting);
    }

    /**
     * Transforms the preprocessed RDF content into trusty URI format and writes it using the provided RDFWriter. The base name for the output can be given as an argument, which can also be a base URI. If not given, the base name is derived from the input file name by removing the extension.
     *
     * @param preprocessedContent the RDF content that has already been preprocessed, which means it has been processed to handle blank nodes, literals, and other aspects according to the specified settings; this content should be ready for generating the artifact code and constructing the trusty URI without needing further preprocessing steps
     * @param baseUri             the base URI to be used
     * @param handler             the RDFHandler to which the transformed RDF statements will be written; this can be an RDFWriter for writing to a file or an RDFHandler for processing the statements in memory
     * @param setting             the settings for the transformation
     * @return the IRI of the transformed RDF content in trusty URI format
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided writer
     */
    public static IRI transformPreprocessed(RdfFileContent preprocessedContent, IRI baseUri, RDFHandler handler, TransformRdfSetting setting)
            throws TrustyUriException {
        ArtifactCode artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
        return includeArtifactCode(preprocessedContent, artifactCode, baseUri, handler, setting);
    }

    /**
     * Includes the artifact code in the RDF content and writes it using the provided RDFWriter or RDFHandler.
     *
     * @param preprocessedContent the RDF content that has already been preprocessed
     * @param artifactCode        the artifact code to be included in the RDF content
     * @param baseUri             the base URI to be used
     * @param writerOrHandler     the RDFWriter or RDFHandler to which the transformed RDF statements will be written
     * @param setting             the settings for the transformation
     * @return the IRI of the transformed RDF content in trusty URI format
     * @throws TrustyUriException if the transformation fails due to an invalid input, issues with the RDF content, or other problems encountered during processing; this exception may be thrown if there are issues with generating the artifact code, constructing the trusty URI, or writing the RDF statements using the provided writer or handler
     */
    public static IRI includeArtifactCode(RdfFileContent preprocessedContent, ArtifactCode artifactCode, IRI baseUri, Object writerOrHandler, TransformRdfSetting setting)
            throws TrustyUriException {
        Map<String, String> ns = makeNamespaceMap(preprocessedContent.getStatements(), baseUri, artifactCode.toString(), setting);
        HashAdder hashAdder;
        if (writerOrHandler instanceof RDFWriter) {
            hashAdder = new HashAdder(baseUri, artifactCode, (RDFWriter) writerOrHandler, ns);
        } else {
            hashAdder = new HashAdder(baseUri, artifactCode, (RDFHandler) writerOrHandler, ns);
        }
        try {
            preprocessedContent.propagate(hashAdder);
        } catch (RDFHandlerException ex) {
            throw new TrustyUriException(ex);
        }
        return RdfUtils.getTrustyUri(baseUri, artifactCode.toString(), setting);

    }

    /**
     * Gets the base URI from the given base name. If the base name contains "://", it is treated as a URI and converted to an IRI. Otherwise, it is considered as a file name and the base URI will be null.
     *
     * @param baseName the base name which can be a URI or a file name; if it contains "://", it will be treated as a URI and converted to an IRI; if it does not contain "://", it will be considered as a file name and the base URI will be null
     * @return the IRI of the base URI if the base name contains "://", or null if the base name is considered as a file name; this IRI can be used as the base URI for constructing trusty URIs and may influence how blank nodes and other elements are handled during transformation
     */
    static IRI getBaseURI(String baseName) {
        IRI baseURI = null;
        if (baseName.indexOf("://") > 0) {
            baseURI = SimpleValueFactory.getInstance().createIRI(baseName);
        }
        return baseURI;
    }

    /**
     * Creates a namespace map for the RDF content based on the given statements, base URI, artifact code, and transformation settings. The namespace map is used to determine how to handle IRIs in the RDF content during transformation, especially those that are related to the trusty URI being generated. The method iterates through the RDF statements and adds relevant IRIs to the namespace map based on whether they start with the trusty URI string constructed from the base URI and artifact code.
     *
     * @param statements   the list of RDF statements from which the namespace map will be constructed
     * @param baseURI      the base URI used
     * @param artifactCode the artifact code used to construct the trusty URI string
     * @param setting      the settings for the transformation, which may influence how the trusty URI string is constructed and how the IRIs are checked against it; these settings may include options for handling blank nodes, literals, and other aspects of the RDF content, which can affect how the namespace map is created based on the IRIs in the statements
     * @return a map containing namespace prefixes and their corresponding URIs based on the RDF statements, base URI, artifact code, and transformation settings; this namespace map can be used during the transformation process to determine how to handle IRIs in the RDF content, especially those that are related to the trusty URI being generated
     */
    static Map<String, String> makeNamespaceMap(List<Statement> statements, IRI baseURI, String artifactCode, TransformRdfSetting setting) {
        Map<String, String> ns = new HashMap<String, String>();
        if (baseURI == null) {
            return ns;
        }
        String u = RdfUtils.getTrustyUriString(baseURI, artifactCode, setting);
        ns.put("this", u);
        for (Statement st : statements) {
            addToNamespaceMap(st.getSubject(), baseURI, artifactCode, ns, setting);
            addToNamespaceMap(st.getPredicate(), baseURI, artifactCode, ns, setting);
            addToNamespaceMap(st.getObject(), baseURI, artifactCode, ns, setting);
            addToNamespaceMap(st.getContext(), baseURI, artifactCode, ns, setting);
        }
        return ns;
    }

    /**
     * Adds the given value to the namespace map if it is an IRI that starts with the trusty URI string constructed from the base URI and artifact code.
     *
     * @param v            the value to be checked and potentially added to the namespace map; this value is expected to be an IRI, and if it starts with the trusty URI string constructed from the base URI and artifact code, it will be added to the namespace map with appropriate prefixes based on its structure; if the value is not an IRI or does not start with the trusty URI string, it will not be added to the namespace map
     * @param baseURI      the base URI used
     * @param artifactCode the artifact code used to construct the trusty URI string; this artifact code is generated from the RDF content and is used to create the trusty URI string that will be checked against the value; if the value starts with this trusty URI string, it may be added to the namespace map with appropriate prefixes
     * @param ns           the namespace map to which the value may be added if it meets the criteria; this map will be updated with new entries if the value is an IRI that starts with the trusty URI string, and the entries will have prefixes such as "node" or "sub" based on the structure of the IRI; if the value does not meet the criteria, the namespace map will remain unchanged
     * @param setting      the settings for the transformation, which may influence how the trusty URI string is constructed and how the value is checked against it; these settings may include options for handling blank nodes, literals, and other aspects of the RDF content, which can affect how the namespace map is updated based on the value
     */
    static void addToNamespaceMap(Value v, IRI baseURI, String artifactCode, Map<String, String> ns, TransformRdfSetting setting) {
        if (!(v instanceof IRI)) {
            return;
        }
        String uri = RdfUtils.getTrustyUriString(baseURI, artifactCode, setting);
        String s = v.toString().replace(" ", artifactCode);
        if (!s.startsWith(uri)) {
            return;
        }
        String suffix = s.substring(uri.length());
        if (suffix.length() > 2 && suffix.charAt(0) == RdfUtils.getPostAcChar(baseURI, setting) && suffix.charAt(1) == setting.getBnodeChar() &&
            !(setting.getBnodeChar() + "").matches("[A-Za-z0-9\\-_]")) {
            ns.put("node", uri + "..");
        } else if (suffix.matches("[^A-Za-z0-9\\-_].*")) {
            ns.put("sub", uri + suffix.charAt(0));
        }
    }

    /**
     * Finalizes the transformation map by replacing the placeholder in the IRIs with the actual artifact code. The transformation map contains mappings from resources to IRIs, where the IRIs may contain a placeholder (e.g., a space) that needs to be replaced with the artifact code. This method iterates through the transformation map, replaces the placeholder in each IRI with the artifact code, and creates a new map with the finalized IRIs.
     *
     * @param transformMap the transformation map containing mappings from resources to IRIs with placeholders; the IRIs in this map may contain a placeholder (e.g., a space) that needs to be replaced with the actual artifact code to finalize the transformation
     * @param artifactCode the artifact code that should replace the placeholder in the IRIs; this artifact code is generated from the RDF content and is used to construct the trusty URI; it will be inserted into the IRIs in place of the placeholder to create the final IRIs that reference the transformed RDF content
     * @return a new map containing the same resources as keys but with IRIs that have the placeholder replaced by the actual artifact code; this finalized transformation map can be used to reference the transformed RDF content with the correct IRIs that include the artifact code
     */
    public static Map<Resource, IRI> finalizeTransformMap(Map<Resource, IRI> transformMap, ArtifactCode artifactCode) {
        Map<Resource, IRI> finalMap = new HashMap<>();
        for (Resource r : transformMap.keySet()) {
            String s = transformMap.get(r).stringValue().replaceFirst(" ", artifactCode.toString());
            finalMap.put(r, SimpleValueFactory.getInstance().createIRI(s));
        }
        return finalMap;
    }

}

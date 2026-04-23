package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Transforms an RDF graph by replacing blank nodes with URIs based on given base URIs, and adding a trusty URI artifact code to the graph.
 */
public class TransformRdfGraph {

    // TODO only transform blank nodes that appear within the given graph

    /**
     * Transforms the RDF graph in the given file by replacing blank nodes with URIs based on the given base URIs, and adding a trusty URI artifact code to the graph.
     *
     * @param args the first argument is the file to transform, and the remaining arguments are either base URIs or files containing base URIs (one per line)
     * @throws IOException        if there is an error reading or writing files
     * @throws TrustyUriException if there is an error during the transformation process, such as an invalid base URI or an error calculating the artifact code
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        if (args.length < 2) {
            throw new RuntimeException("Not enough arguments: <file> <graph-uri1> (<graph-uri2> ...)");
        }
        File inputFile = new File(args[0]);
        List<IRI> baseUris = new ArrayList<IRI>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("://")) {
                baseUris.add(SimpleValueFactory.getInstance().createIRI(arg));
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(arg));
                String line;
                while ((line = reader.readLine()) != null) {
                    baseUris.add(SimpleValueFactory.getInstance().createIRI(line));
                }
                reader.close();
            }
        }
        RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
        String outputFilePath = inputFile.getPath().replaceFirst("[.][^.]+$", "") + ".t";
        RDFFormat format = content.getOriginalFormat();
        if (!format.getFileExtensions().isEmpty()) {
            outputFilePath += "." + format.getFileExtensions().get(0);
        }
        transform(content, new File(outputFilePath), TransformRdfSetting.defautSetting, baseUris.toArray(new IRI[baseUris.size()]));
    }

    /**
     * Transforms the RDF graph in the given content by replacing blank nodes with URIs based on the given base URIs, and adding a trusty URI artifact code to the graph, and writes the transformed graph to the given output file.
     *
     * @param content    the RDF graph content to transform
     * @param outputFile the file to write the transformed RDF graph to
     * @param setting    the settings to use for the transformation, such as whether to use blank node identifiers or not
     * @param baseUris   the base URIs to use for replacing blank nodes with URIs, where each base URI will be used in a separate transformation step, and the resulting graph from each step will be used as the input for the next step
     * @throws IOException        if there is an error writing the output file
     * @throws TrustyUriException if there is an error during the transformation process, such as an invalid base URI or an error calculating the artifact code
     */
    public static void transform(RdfFileContent content, File outputFile, TransformRdfSetting setting, IRI... baseUris)
            throws IOException, TrustyUriException {
        try {
            OutputStream out = new FileOutputStream(outputFile);
            processBaseUris(content, Rio.createWriter(content.getOriginalFormat(), new OutputStreamWriter(out, Charset.forName("UTF-8"))), setting, baseUris);
            out.close();
        } catch (RDFHandlerException ex) {
            throw new TrustyUriException(ex);
        }
    }

    public static void transform(RdfFileContent content, RDFHandler handler, TransformRdfSetting setting, IRI... baseUris)
            throws IOException, TrustyUriException {
        try {
            processBaseUris(content, handler, setting, baseUris);
        } catch (RDFHandlerException ex) {
            throw new TrustyUriException(ex);
        }
    }

    public static void transform(InputStream in, RDFFormat format, OutputStream out, TransformRdfSetting setting, IRI... baseUris)
            throws IOException, TrustyUriException {
        RdfFileContent content = RdfUtils.load(in, format);
        try {
            processBaseUris(content, Rio.createWriter(format, new OutputStreamWriter(out, Charset.forName("UTF-8"))), setting, baseUris);
        } catch (RDFHandlerException ex) {
            throw new TrustyUriException(ex);
        }
        out.close();
    }

    private static void processBaseUris(RdfFileContent content, RDFHandler handler, TransformRdfSetting setting, IRI... baseUris)
            throws RDFHandlerException, TrustyUriException {
        for (IRI baseUri : baseUris) {
            RdfFileContent newContent = new RdfFileContent(content.getOriginalFormat());
            RdfPreprocessor preprocessor = new RdfPreprocessor(newContent, baseUri, setting);
            content.propagate(preprocessor);
            content = newContent;
            ArtifactCode artifactCode = RdfHasher.makeGraphArtifactCode(content.getStatements(), baseUri, setting);
            newContent = new RdfFileContent(content.getOriginalFormat());
            HashAdder hashAdder = new HashAdder(baseUri, artifactCode, newContent, null);
            content.propagate(hashAdder);
            content = newContent;
        }
        content.propagate(handler);
    }

}

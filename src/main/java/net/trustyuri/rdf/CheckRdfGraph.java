package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import net.trustyuri.TrustyUriUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to check an RDF graph.
 */
public class CheckRdfGraph {

    private static final Logger logger = LoggerFactory.getLogger(CheckRdfGraph.class);

    /**
     * Checks the given RDF graph(s).
     *
     * @param args the first argument is the file containing the RDF graph, the following arguments are the graph URIs to check
     * @throws IOException        if there is an error reading the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public static void main(String[] args) throws IOException, TrustyUriException {
        if (args.length < 2) {
            throw new RuntimeException("Not enough arguments: <file> <graph-uri1> (<graph-uri2> ...)");
        }
        String fileName = args[0];
        CheckRdfGraph c;
        try {
            URL url = new URL(fileName);
            c = new CheckRdfGraph(url);
        } catch (MalformedURLException ex) {
            c = new CheckRdfGraph(new File(fileName));
        }
        for (int i = 1; i < args.length; i++) {
            IRI graphUri = SimpleValueFactory.getInstance().createIRI(args[i]);
            boolean valid = c.check(graphUri);
            if (valid) {
                logger.info("Correct hash: {}", getArtifactCode(graphUri));
            } else {
                logger.error("*** INCORRECT HASH ***");
            }
        }
    }

    private TrustyUriResource r;
    private RdfFileContent content;

    /**
     * Creates a new CheckRdfGraph object for the given URL.
     *
     * @param url the URL of the RDF graph to check
     * @throws IOException        if there is an error reading the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public CheckRdfGraph(URL url) throws IOException, TrustyUriException {
        r = new TrustyUriResource(url);
        init();
    }

    /**
     * Creates a new CheckRdfGraph object for the given file.
     *
     * @param file the file containing the RDF graph to check
     * @throws IOException        if there is an error reading the file
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the file is not a trusty file or if the module is unknown
     */
    public CheckRdfGraph(File file) throws IOException, TrustyUriException {
        r = new TrustyUriResource(file);
        init();
    }

    private void init() throws IOException, TrustyUriException {
        content = RdfUtils.load(r);
    }

    /**
     * Checks the graph with the given graph URI.
     *
     * @param graphUri the graph URI of the graph to check
     * @return true if the hash is correct, false otherwise
     * @throws TrustyUriException if there is an error with the trusty URI, for example if the graph URI is not a trusty URI or if the module is unknown
     */
    public boolean check(IRI graphUri) throws TrustyUriException {
        ArtifactCode artifactCode = getArtifactCode(graphUri);
        if (!artifactCode.getModule().getModuleId().equals(RdfGraphModule.MODULE_ID)) {
            throw new TrustyUriException("Not a trusty URI of type " + RdfGraphModule.MODULE_ID + ": " + graphUri);
        }
        List<Statement> graph = new ArrayList<>();
        for (Statement st : content.getStatements()) {
            if (graphUri.equals(st.getContext())) {
                graph.add(st);
            }
        }
        graph = RdfPreprocessor.run(graph, artifactCode.toString());
        ArtifactCode ac = RdfHasher.makeGraphArtifactCode(graph);
        return artifactCode.equals(ac);
    }

    private static ArtifactCode getArtifactCode(IRI graphUri) {
        return ArtifactCode.of(TrustyUriUtils.getArtifactCode(graphUri.stringValue()));
    }

}

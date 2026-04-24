package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.HashMap;
import java.util.Map;

/**
 * RDFHandler that adds a hash to all IRIs in the graph, based on the provided artifact code.
 */
public class HashAdder implements RDFHandler {

    private IRI baseURI;
    private ArtifactCode artifactCode;
    private RDFHandler handler;
    private Map<String, String> ns;
    private Map<IRI, IRI> transformMap;

    /**
     * Creates a new HashAdder.
     *
     * @param baseURI      the base URI to use for the graph
     * @param artifactCode the artifact code to use
     * @param handler      the RDFHandler to delegate to
     * @param ns           a map of namespace prefixes to URIs, which will be passed to the handler
     */
    public HashAdder(IRI baseURI, ArtifactCode artifactCode, RDFHandler handler, Map<String, String> ns) {
        this.baseURI = baseURI;
        this.artifactCode = artifactCode;
        this.handler = handler;
        this.ns = ns;
        if (ns == null) {
            this.ns = new HashMap<>();
        }
        transformMap = new HashMap<>();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
        if (ns.get("this") != null) {
            handler.handleNamespace("this", ns.get("this"));
        }
        if (ns.get("sub") != null) {
            handler.handleNamespace("sub", ns.get("sub"));
        }
        if (ns.get("node") != null) {
            handler.handleNamespace("node", ns.get("node"));
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        handler.endRDF();
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        if (baseURI != null && baseURI.toString().equals(uri)) {
            return;
        }
        handler.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        Resource context = transform(st.getContext());
        Resource subject = transform(st.getSubject());
        IRI predicate = transform(st.getPredicate());
        Value object = st.getObject();
        if (object instanceof Resource) {
            object = transform((Resource) object);
        }
        Statement n = SimpleValueFactory.getInstance().createStatement(subject, predicate, object, context);
        handler.handleStatement(n);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        handler.handleComment(comment);
    }

    private IRI transform(Resource r) {
        if (r == null) {
            return null;
        } else if (r instanceof BNode) {
            throw new RuntimeException("Unexpected blank node encountered");
        } else {
            IRI transformedURI = SimpleValueFactory.getInstance().createIRI(r.toString().replace(" ", artifactCode.toString()));
            transformMap.put((IRI) r, transformedURI);
            return transformedURI;
        }
    }

    /**
     * Returns a map of the original IRIs to the transformed IRIs.
     *
     * @return a map of the original IRIs to the transformed IRIs
     */
    public Map<IRI, IRI> getTransformMap() {
        return transformMap;
    }

}

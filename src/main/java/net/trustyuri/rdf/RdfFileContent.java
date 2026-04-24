package net.trustyuri.rdf;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the content of an RDF file.
 */
public class RdfFileContent implements RDFHandler {

    private static final Logger logger = LoggerFactory.getLogger(RdfFileContent.class);

    private final Map<Value, Value> rdfEntityMap = new HashMap<>();

    private RDFFormat originalFormat = null;
    private List<Pair<String, String>> namespaces;
    private List<Statement> statements;

    /**
     * Creates a new RdfFileContent object with the given original format.
     *
     * @param originalFormat the original format of the RDF file, or null if the format is unknown
     */
    public RdfFileContent(RDFFormat originalFormat) {
        this.originalFormat = originalFormat;
        logger.debug("Created RdfFileContent with original format: {}", originalFormat);
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        namespaces = new ArrayList<>();
        statements = new ArrayList<>();
        logger.debug("startRDF: initialized namespaces and statements containers");
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        int nsCount = namespaces == null ? 0 : namespaces.size();
        int stCount = statements == null ? 0 : statements.size();
        logger.debug("endRDF: completed reading RDF content (namespaces={}, statements={})", nsCount, stCount);
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        namespaces.add(Pair.of(prefix, uri));
        logger.debug("handleNamespace: added namespace prefix='{}' uri='{}' (total namespaces={})", prefix, uri, namespaces.size());
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        logger.trace("Processing statement {}", st);
        Resource subj = (Resource) rdfEntityMap.get(st.getSubject());
        if (subj == null) {
            subj = st.getSubject();
            rdfEntityMap.put(subj, subj);
            logger.trace("Added subject {} to rdfEntityMap (total entities={})", subj, rdfEntityMap.size());
        }
        IRI pred = (IRI) rdfEntityMap.get(st.getPredicate());
        if (pred == null) {
            pred = st.getPredicate();
            rdfEntityMap.put(pred, pred);
            logger.trace("Added predicate {} to rdfEntityMap (total entities={})", pred, rdfEntityMap.size());
        }
        Value obj = (Value) rdfEntityMap.get(st.getObject());
        if (obj == null) {
            obj = st.getObject();
            rdfEntityMap.put(obj, obj);
            logger.trace("Added object {} to rdfEntityMap (total entities={})", obj, rdfEntityMap.size());
        }
        Resource context;
        if (st.getContext() == null) {
            st = SimpleValueFactory.getInstance().createStatement(subj, pred, obj);
        } else {
            context = (Resource) rdfEntityMap.get(st.getContext());
            if (context == null) {
                context = st.getContext();
                rdfEntityMap.put(context, context);
                logger.trace("Added context {} to rdfEntityMap (total entities={})", context, rdfEntityMap.size());
            }
            st = SimpleValueFactory.getInstance().createStatement(subj, pred, obj, context);
        }

        statements.add(st);
        logger.debug("handleStatement: added statement {} (total statements={})", st, statements.size());
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // Ignore comments
        logger.trace("Ignored comment '{}'", comment);
    }

    /**
     * Returns the statements of the RDF file.
     *
     * @return the statements of the RDF file
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Returns the namespaces of the RDF file.
     *
     * @return the namespaces of the RDF file
     */
    public List<Pair<String, String>> getNamespaces() {
        return namespaces;
    }

    /**
     * Propagates the content of the RDF file to the given RDF handler.
     *
     * @param handler the RDF handler to propagate the content to
     * @throws RDFHandlerException if there is an error propagating the content
     */
    public void propagate(RDFHandler handler) throws RDFHandlerException {
        propagate(handler, true);
    }

    /**
     * Propagates the content of the RDF file to the given RDF handler.
     *
     * @param handler       the RDF handler to propagate the content to
     * @param doStartAndEnd whether to call startRDF() and endRDF() on the handler
     * @throws RDFHandlerException if there is an error propagating the content
     */
    public void propagate(RDFHandler handler, boolean doStartAndEnd) throws RDFHandlerException {
        int nsCount = namespaces == null ? 0 : namespaces.size();
        int stCount = statements == null ? 0 : statements.size();
        logger.debug("propagate: starting propagation to handler {} (doStartAndEnd={}, namespaces={}, statements={})", handler, doStartAndEnd, nsCount, stCount);
        if (doStartAndEnd) {
            handler.startRDF();
            logger.trace("propagate: called startRDF() on handler {}", handler);
        }
        for (Pair<String, String> ns : namespaces) {
            handler.handleNamespace(ns.getLeft(), ns.getRight());
        }
        Statement prev = null;
        for (Statement st : statements) {
            // omitting duplicates
            if (prev != null && prev.equals(st)) {
                logger.debug("propagate: skipping duplicate statement {}", st);
                continue;
            }
            prev = st;
            handler.handleStatement(st);
        }
        if (doStartAndEnd) {
            handler.endRDF();
            logger.trace("propagate: called handler.endRDF()");
        }
        logger.debug("propagate: completed propagation to handler {}", handler);
    }

    /**
     * Returns the original format of the RDF file, or null if the format is unknown.
     *
     * @return the original format of the RDF file, or null if the format is unknown
     */
    public RDFFormat getOriginalFormat() {
        logger.debug("getOriginalFormat: returning original format {}", originalFormat);
        return originalFormat;
    }

}

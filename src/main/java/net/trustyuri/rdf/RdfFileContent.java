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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the content of an RDF file.
 */
public class RdfFileContent implements RDFHandler {

    private Map<Value, Value> rdfEntityMap = new HashMap<Value, Value>();

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
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        namespaces = new ArrayList<Pair<String, String>>();
        statements = new ArrayList<Statement>();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        namespaces.add(Pair.of(prefix, uri));
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        Resource subj = (Resource) rdfEntityMap.get(st.getSubject());
        if (subj == null) {
            subj = st.getSubject();
            rdfEntityMap.put(subj, subj);
        }
        IRI pred = (IRI) rdfEntityMap.get(st.getPredicate());
        if (pred == null) {
            pred = st.getPredicate();
            rdfEntityMap.put(pred, pred);
        }
        Value obj = (Value) rdfEntityMap.get(st.getObject());
        if (obj == null) {
            obj = st.getObject();
            rdfEntityMap.put(obj, obj);
        }
        Resource context;
        if (st.getContext() == null) {
            st = SimpleValueFactory.getInstance().createStatement(subj, pred, obj);
        } else {
            context = (Resource) rdfEntityMap.get(st.getContext());
            if (context == null) {
                context = st.getContext();
                rdfEntityMap.put(context, context);
            }
            st = SimpleValueFactory.getInstance().createStatement(subj, pred, obj, context);
        }

        statements.add(st);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // Ignore comments
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
        if (doStartAndEnd) {
            handler.startRDF();
        }
        for (Pair<String, String> ns : namespaces) {
            handler.handleNamespace(ns.getLeft(), ns.getRight());
        }
        Statement prev = null;
        for (Statement st : statements) {
            // omitting duplicates
            if (prev != null && prev.equals(st)) {
                continue;
            }
            prev = st;
            handler.handleStatement(st);
        }
        if (doStartAndEnd) {
            handler.endRDF();
        }
    }

    /**
     * Returns the original format of the RDF file, or null if the format is unknown.
     *
     * @return the original format of the RDF file, or null if the format is unknown
     */
    public RDFFormat getOriginalFormat() {
        return originalFormat;
    }

}

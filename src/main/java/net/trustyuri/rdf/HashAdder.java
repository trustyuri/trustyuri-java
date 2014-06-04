package net.trustyuri.rdf;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class HashAdder implements RDFHandler {

	private URI baseURI;
	private String artifactCode;
	private RDFHandler handler;
	private Map<String,String> ns;

	public HashAdder(URI baseURI, String artifactCode, RDFHandler handler, Map<String,String> ns) {
		this.baseURI = baseURI;
		this.artifactCode = artifactCode;
		this.handler = handler;
		this.ns = ns;
		if (ns == null) {
			this.ns = new HashMap<String,String>();
		}
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		handler.startRDF();
		if (ns.get("this") != null) handler.handleNamespace("this", ns.get("this"));
		if (ns.get("sub") != null) handler.handleNamespace("sub", ns.get("sub"));
		if (ns.get("node") != null) handler.handleNamespace("node", ns.get("node"));
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
		URI predicate = transform(st.getPredicate());
		Value object = st.getObject();
		if (object instanceof Resource) {
			object = transform((Resource) object);
		}
		Statement n = new ContextStatementImpl(subject, predicate, object, context);
		handler.handleStatement(n);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		handler.handleComment(comment);
	}

	private URI transform(Resource r) {
		if (r == null) {
			return null;
		} else if (r instanceof BNode) {
			throw new RuntimeException("Unexpected blank node encountered");
		} else {
			return new URIImpl(r.toString().replace(" ", artifactCode));
		}
	}

}

package ch.tkuhn.hashuri.rdf;

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
	private String hash;
	private RDFHandler handler;
	private Map<String,String> ns;

	public HashAdder(URI baseURI, String hash, RDFHandler handler, Map<String,String> ns) {
		this.baseURI = baseURI;
		this.hash = hash;
		this.handler = handler;
		this.ns = ns;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		handler.startRDF();
		if (ns.get("this") != null) handler.handleNamespace("this", ns.get("this"));
		if (ns.get("sub") != null) handler.handleNamespace("sub", ns.get("sub"));
		if (ns.get("blank") != null) handler.handleNamespace("blank", ns.get("blank"));
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
			return new URIImpl(r.toString().replaceAll(" ", hash));
		}
	}

}

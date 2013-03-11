package ch.tkuhn.hashrdf;

import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class HashAdder implements RDFHandler {

	private URI baseURI;
	private String hash;
	private RDFHandler handler;
	private Map<String,Integer> blankNodeMap;

	public HashAdder(URI baseURI, String hash, RDFHandler handler, Map<String,Integer> blankNodeMap) {
		this.baseURI = baseURI;
		this.hash = hash;
		this.handler = handler;
		this.blankNodeMap = blankNodeMap;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		handler.startRDF();
		if (baseURI != null) {
			handler.handleNamespace("this", HashURIUtils.getHashURIString(baseURI, hash));
			handler.handleNamespace("sub", HashURIUtils.getHashURIString(baseURI, hash, ""));
			handler.handleNamespace("blank", HashURIUtils.getHashURIString(baseURI, hash, "") + ".");
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
		if (baseURI != null) {
			return HashURIUtils.getHashURI(r, baseURI, hash, blankNodeMap);
		} else if (r instanceof BNode) {
			throw new RuntimeException("Unexpected blank node encountered");
		} else {
			return (URI) r;
		}
	}

}

package ch.tkuhn.hashrdf;

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

	public HashAdder(URI baseURI, String hash, RDFHandler handler) {
		this.baseURI = baseURI;
		this.hash = hash;
		this.handler = handler;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		handler.startRDF();
		if (baseURI != null) {
			handler.handleNamespace("this", HashURIUtils.getHashURIString(baseURI, hash));
			handler.handleNamespace("sub", HashURIUtils.getHashURIString(baseURI, hash, ""));
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
		Resource context = st.getContext();
		if (context instanceof URI) {
			context = replace((URI) context);
		}
		Resource subject = st.getSubject();
		if (subject instanceof URI) {
			subject = replace((URI) subject);
		}
		URI predicate = replace(st.getPredicate());
		Value object = st.getObject();
		if (object instanceof URI) {
			object = replace((URI) object);
		}
		Statement n = new ContextStatementImpl(subject, predicate, object, context);
		handler.handleStatement(n);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		handler.handleComment(comment);
	}

	private URI replace(URI uri) {
		if (baseURI != null) {
			return HashURIUtils.getHashURI(uri, baseURI, hash);
		} else {
			return uri;
		}
	}

}

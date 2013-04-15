package ch.tkuhn.hashuri.rdf;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class RdfPreprocessor implements RDFHandler {

	private RDFHandler nestedHandler;
	private URI baseUri;
	private Map<String,Integer> blankNodeMap = new HashMap<>();

	public RdfPreprocessor(RDFHandler nestedHandler, URI baseUri) {
		this.nestedHandler = nestedHandler;
		this.baseUri = baseUri;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		nestedHandler.startRDF();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		nestedHandler.endRDF();
	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
		nestedHandler.handleNamespace(prefix, uri);
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
		nestedHandler.handleStatement(n);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		nestedHandler.handleComment(comment);
	}

	private URI transform(Resource r) {
		return RdfUtils.getHashURI(r, baseUri, " ", blankNodeMap);
	}

}

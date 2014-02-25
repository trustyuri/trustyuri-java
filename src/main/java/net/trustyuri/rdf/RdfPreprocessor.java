package net.trustyuri.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class RdfPreprocessor implements RDFHandler {

	// TODO check that already hashed graphs are not changed

	private RDFHandler nestedHandler;
	private URI baseUri;
	private String hash;
	private Map<String,Integer> blankNodeMap;

	public static RdfFileContent run(RdfFileContent content, URI baseUri) throws RDFHandlerException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		content.propagate(new RdfPreprocessor(p, baseUri));
		return p;
	}

	public static RdfFileContent run(RdfFileContent content, String hash) throws RDFHandlerException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		content.propagate(new RdfPreprocessor(p, hash));
		return p;
	}

	public static List<Statement> run(List<Statement> statements, URI baseUri) {
		return run(statements, baseUri, null);
	}

	public static List<Statement> run(List<Statement> statements, String hash) {
		return run(statements, null, hash);
	}

	private static List<Statement> run(List<Statement> statements, URI baseUri, String hash) {
		List<Statement> r = new ArrayList<>();
		RdfPreprocessor obj = new RdfPreprocessor(baseUri, hash);
		for (Statement st : statements) {
			r.add(obj.preprocess(st));
		}
		return r;
	}

	private RdfPreprocessor(URI baseUri, String hash) {
		this.baseUri = baseUri;
		this.hash = hash;
		this.blankNodeMap = new HashMap<>();
	}

	public RdfPreprocessor(RDFHandler nestedHandler, URI baseUri) {
		this(nestedHandler, baseUri, null);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String hash) {
		this(nestedHandler, hash, null);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, URI baseUri, Map<String,Integer> blankNodeMap) {
		this.nestedHandler = nestedHandler;
		this.baseUri = baseUri;
		this.hash = null;
		this.blankNodeMap = blankNodeMap;
		if (blankNodeMap == null) {
			this.blankNodeMap = new HashMap<>();
		}
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String hash, Map<String,Integer> blankNodeMap) {
		this.nestedHandler = nestedHandler;
		this.baseUri = null;
		this.hash = hash;
		this.blankNodeMap = blankNodeMap;
		if (blankNodeMap == null) {
			this.blankNodeMap = new HashMap<>();
		}
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
		nestedHandler.handleStatement(preprocess(st));
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		nestedHandler.handleComment(comment);
	}

	private Statement preprocess(Statement st) {
		Resource context = null;
		if (st.getContext() != null) {
			context = transform(st.getContext());
		}
		Resource subject = transform(st.getSubject());
		URI predicate = transform(st.getPredicate());
		Value object = st.getObject();
		if (object instanceof Resource) {
			object = transform((Resource) object);
		}
		return new ContextStatementImpl(subject, predicate, object, context);
	}

	private URI transform(Resource r) {
		if (baseUri == null) {
			return new URIImpl(RdfUtils.normalize((URI) r, hash));
		}
		return RdfUtils.getTrustyUri(r, baseUri, " ", blankNodeMap);
	}

}

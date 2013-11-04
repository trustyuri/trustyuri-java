package ch.tkuhn.hashuri.rdf;

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

	private RDFHandler nestedHandler;
	private URI baseUri;
	private String hash;
	private Map<String,Integer> blankNodeMap;

	public static RdfFileContent run(RdfFileContent content, URI baseUri,
			Map<String,Integer> blankNodeMap) throws RDFHandlerException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		content.propagate(new RdfPreprocessor(p, baseUri, blankNodeMap));
		return p;
	}

	public static RdfFileContent run(RdfFileContent content, String hash,
			Map<String,Integer> blankNodeMap) throws RDFHandlerException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		content.propagate(new RdfPreprocessor(p, hash, blankNodeMap));
		return p;
	}

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
		Map<String,Integer> blankNodeMap = new HashMap<>();
		for (Statement st : statements) {
			r.add(preprocess(st, baseUri, hash, blankNodeMap));
		}
		return r;
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
		Statement n = preprocess(st, baseUri, hash, blankNodeMap);
		nestedHandler.handleStatement(n);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		nestedHandler.handleComment(comment);
	}

	public static Statement preprocess(Statement st, URI baseUri, String hash, Map<String,Integer> blankNodeMap) {
		Resource context = null;
		if (st.getContext() != null) {
			context = transform(st.getContext(), baseUri, hash, blankNodeMap);
		}
		Resource subject = transform(st.getSubject(), baseUri, hash, blankNodeMap);
		URI predicate = transform(st.getPredicate(), baseUri, hash, blankNodeMap);
		Value object = st.getObject();
		if (object instanceof Resource) {
			object = transform((Resource) object, baseUri, hash, blankNodeMap);
		}
		return new ContextStatementImpl(subject, predicate, object, context);
	}

	public static URI transform(Resource r, URI baseUri, String hash, Map<String,Integer> blankNodeMap) {
		if (baseUri == null) {
			return new URIImpl(RdfUtils.normalize((URI) r, hash));
		}
		return RdfUtils.getHashURI(r, baseUri, " ", blankNodeMap);
	}

	public static URI transformResource(Resource r, URI baseUri, Map<String,Integer> blankNodeMap) {
		if (r instanceof URI && ((URI) r).toString().indexOf(" ") > -1) {
			return (URI) r;
		}
		return RdfUtils.getHashURI(r, baseUri, " ", blankNodeMap);
	}

}

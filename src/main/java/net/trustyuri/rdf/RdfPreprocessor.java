package net.trustyuri.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriModule;

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
	private String artifactCode;
	private Map<String,Integer> blankNodeMap;
	private TrustyUriModule moduleRB;
	private Map<Resource,URI> transformMap;

	public static RdfFileContent run(RdfFileContent content, URI baseUri) throws TrustyUriException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		try {
			content.propagate(new RdfPreprocessor(p, baseUri));
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		return p;
	}

	public static RdfFileContent run(RdfFileContent content, String artifactCode) throws TrustyUriException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		try {
			content.propagate(new RdfPreprocessor(p, artifactCode));
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		return p;
	}

	public static List<Statement> run(List<Statement> statements, URI baseUri) {
		return run(statements, baseUri, null);
	}

	public static List<Statement> run(List<Statement> statements, String artifactCode) {
		return run(statements, null, artifactCode);
	}

	private static List<Statement> run(List<Statement> statements, URI baseUri, String artifactCode) {
		List<Statement> r = new ArrayList<Statement>();
		RdfPreprocessor obj = new RdfPreprocessor(baseUri, artifactCode);
		for (Statement st : statements) {
			r.add(obj.preprocess(st));
		}
		return r;
	}

	private RdfPreprocessor(URI baseUri, String artifactCode) {
		this.baseUri = baseUri;
		this.artifactCode = artifactCode;
		init();
	}

	public RdfPreprocessor(RDFHandler nestedHandler, URI baseUri) {
		this(nestedHandler, baseUri, null);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String artifactCode) {
		this(nestedHandler, artifactCode, null);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, URI baseUri, Map<String,Integer> blankNodeMap) {
		this.nestedHandler = nestedHandler;
		this.baseUri = baseUri;
		this.artifactCode = null;
		this.blankNodeMap = blankNodeMap;
		init();
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String artifactCode, Map<String,Integer> blankNodeMap) {
		this.nestedHandler = nestedHandler;
		this.baseUri = null;
		this.artifactCode = artifactCode;
		this.blankNodeMap = blankNodeMap;
		init();
	}

	private void init() {
		if (blankNodeMap == null) {
			this.blankNodeMap = new HashMap<String,Integer>();
		}
		moduleRB = ModuleDirectory.getModule(RdfGraphModule.MODULE_ID);
		transformMap = new HashMap<Resource,URI>();
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

	public Map<Resource,URI> getTransformMap() {
		return transformMap;
	}

	private Statement preprocess(Statement st) {
		Resource context = st.getContext();
		URI trustyGraph = null;
		if (context != null) {
			if (context instanceof URI && moduleRB.matches((URI) context)) {
				trustyGraph = (URI) context;
			}
			context = transform(context, trustyGraph);
		}
		Resource subject = transform(st.getSubject(), trustyGraph);
		URI predicate = transform(st.getPredicate(), trustyGraph);
		Value object = st.getObject();
		if (object instanceof Resource) {
			object = transform((Resource) object, trustyGraph);
		}
		return new ContextStatementImpl(subject, predicate, object, context);
	}

	private URI transform(Resource r, URI trustyGraph) {
		if (baseUri == null) {
			return new URIImpl(RdfUtils.normalize((URI) r, artifactCode));
		}
		URI uri = RdfUtils.getPreUri(r, baseUri, blankNodeMap, trustyGraph != null);
		if (uri == null) {
			// TODO Allow for 'force' option; URI might only look like a trusty URI...
			throw new RuntimeException("Transformation would break existing trusty URI graph: " +
					trustyGraph);
		} else if (!r.toString().equals(uri.toString())) {
			transformMap.put(r, uri);
		}
		return uri;
	}

}
